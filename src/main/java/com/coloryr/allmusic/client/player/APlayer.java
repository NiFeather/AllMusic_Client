package com.coloryr.allmusic.client.player;

import com.coloryr.allmusic.client.AllMusic;
import com.coloryr.allmusic.client.player.decoder.BuffPack;
import com.coloryr.allmusic.client.player.decoder.IDecoder;
import com.coloryr.allmusic.client.player.decoder.flac.FlacDecoder;
import com.coloryr.allmusic.client.player.decoder.mp3.Mp3Decoder;
import com.coloryr.allmusic.client.player.decoder.ogg.OggDecoder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Queue;
import java.util.concurrent.*;

public class APlayer extends InputStream {

    private static final Logger log = LoggerFactory.getLogger(APlayer.class);
    private final Queue<String> urls = new ConcurrentLinkedQueue<>();
    private final Semaphore semaphore = new Semaphore(0);
    private final Semaphore semaphore1 = new Semaphore(0);
    private HttpClient client;
    private String url;
    private HttpGet get;
    private InputStream content;
    private boolean isClose = false;
    private boolean reload = false;
    private IDecoder decoder;
    private int time = 0;
    private long local = 0;
    private boolean isPlay = false;
    private boolean wait = false;
    private int alSourceName;
    private int frequency;
    private int channels;

    // 待播放的，已经解码的音频块
    private final Queue<ByteBuffer> bufferQueue = new ConcurrentLinkedQueue<>();

    public APlayer()
    {
        try {
            new Thread(this::playerLoop, "AllMusic Player Loop Thread").start();
            client = HttpClientBuilder.create()
                    .useSystemProperties()
                    .build();

            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            service.scheduleAtFixedRate(this::run1, 0, 10, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run1()
    {
        if (isPlay)
        {
            time += 10;
        }
    }

    public boolean isPlay()
    {
        return isPlay;
    }

    public String Get(String url)
    {
        if (url.contains("https://music.163.com/song/media/outer/url?id=")
                || url.contains("http://music.163.com/song/media/outer/url?id="))
        {
            try
            {
                HttpGet get = new HttpGet(url);
                get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36 Edg/84.0.522.52");
                get.setHeader("Host", "music.163.com");
                HttpResponse response = client.execute(get);
                StatusLine line = response.getStatusLine();
                if (line.getStatusCode() == 302)
                {
                    return response.getFirstHeader("Location").getValue();
                }
                return url;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return url;
    }

    public void set(String time)
    {
        try
        {
            int time1 = Integer.parseInt(time);
            set(time1);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void set(int time) {
        if (url == null) {
            return;
        }
        closePlayer();
        this.time = time;
        urls.add(url);
        semaphore.release();
    }

    public void connect() throws IOException {
        getClose();
        streamClose();
        get = new HttpGet(url);
        get.setHeader("Range", "bytes=" + local + "-");
        HttpResponse response = this.client.execute(get);
        HttpEntity entity = response.getEntity();
        content = entity.getContent();
    }

    private void playerLoop() {
        while (true) {
            try {
                semaphore.acquire();
                url = urls.poll();
                if (url == null || url.isEmpty()) continue;
                urls.clear();
                url = Get(url);
                if (url == null) continue;
                try {
                    local = 0;
                    connect();
                } catch (Exception e) {
                    e.printStackTrace();
                    AllMusic.sendMessage("[AllMusic客户端]获取音乐失败");
                    continue;
                }

                decoder = new FlacDecoder(this);
                if (!decoder.set()) {
                    local = 0;
                    connect();
                    decoder = new OggDecoder(this);
                    if (!decoder.set()) {
                        local = 0;
                        connect();
                        decoder = new Mp3Decoder(this);
                        if (!decoder.set()) {
                            AllMusic.sendMessage("[AllMusic客户端]不支持这样的文件播放");
                            continue;
                        }
                    }
                }

                MinecraftClient.getInstance().player.sendMessage(Text.literal("Decoder is " + decoder), false);

                isPlay = true;

                alSourceName = AL10.alGenSources();
                int m_numqueued = AL10.alGetSourcei(alSourceName, AL10.AL_BUFFERS_QUEUED);
                while (m_numqueued > 0) {
                    int temp = AL10.alSourceUnqueueBuffers(alSourceName);
                    AL10.alDeleteBuffers(temp);
                    m_numqueued--;
                }
                frequency = decoder.getOutputFrequency();
                channels = decoder.getOutputChannels();
                if (channels != 1 && channels != 2) continue;
                if (time != 0) {
                    decoder.set(time);
                }
                bufferQueue.clear();
                reload = false;
                isClose = false;

                // 解码循环
                while (true)
                {
                    try
                    {
                        if (isClose) break;

                        // 解码音频块，并丢到queue中
                        // 一次性多解码点丢缓冲区，因为原本的 50 可能会导致FLAC播放抽搐（频繁seek到之前播放的地方）？
                        var maxBufferQueueSize = 500; //AllMusic.instance().configurations.currentConfig.queueSize;
                        while (AL10.alGetSourcei(alSourceName, AL10.AL_BUFFERS_QUEUED) < maxBufferQueueSize)
                        {
                            BuffPack output = decoder.decodeFrame();
                            if (output == null) break;
                            ByteBuffer byteBuffer = BufferUtils.createByteBuffer(output.len)
                                    .put(output.buff, 0, output.len);
                            ((Buffer) byteBuffer).flip();
                            bufferQueue.add(byteBuffer);

                            AL10.alSourcef(alSourceName, AL10.AL_GAIN, AllMusic.getVolume());
                        }

                        AL10.alSourcef(alSourceName, AL10.AL_GAIN, AllMusic.getVolume());

                        if (AL10.alGetSourcei(alSourceName, AL10.AL_BUFFERS_PROCESSED) > 0)
                        {
                            int temp = AL10.alSourceUnqueueBuffers(alSourceName);
                            AL10.alDeleteBuffers(temp);
                        }

                        // 不要占用那么多资源去循环，休息一下
                        Thread.sleep(10);
                    }
                    catch (Throwable t)
                    {
                        if (!isClose)
                        {
                            log.error("执行解码循环时出现问题：" + t.getMessage());
                            t.printStackTrace();
                        }

                        break;
                    }
                }

                getClose();
                streamClose();
                decodeClose();

                while (!isClose && AL10.alGetSourcei(alSourceName, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING)
                {
                    AL10.alSourcef(alSourceName, AL10.AL_GAIN, AllMusic.getVolume());
                    Thread.sleep(50);
                }

                if (!reload) {
                    wait = true;
                    if (semaphore1.tryAcquire(500, TimeUnit.MILLISECONDS)) {
                        if (reload) {
                            urls.add(url);
                            semaphore.release();
                            continue;
                        }
                    }
                    isPlay = false;
                    AL10.alSourceStop(alSourceName);
                    m_numqueued = AL10.alGetSourcei(alSourceName, AL10.AL_BUFFERS_QUEUED);
                    while (m_numqueued > 0) {
                        int temp = AL10.alSourceUnqueueBuffers(alSourceName);
                        AL10.alDeleteBuffers(temp);
                        m_numqueued--;
                    }
                    AL10.alDeleteSources(alSourceName);
                } else {
                    urls.add(url);
                    semaphore.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void tick()
    {
        int count = 0;

        if (wait)
        {
            wait = false;
            semaphore1.release();
        }

        if (isClose)
        {
            bufferQueue.clear();
            return;
        }

        while (!bufferQueue.isEmpty())
        {
            count++;

            var config = AllMusic.instance().configurations.currentConfig;

            if (count > config.exitSize) break;
            ByteBuffer byteBuffer = bufferQueue.poll();

            //MinecraftClient.getInstance().player.sendMessage(Text.literal("Count %s ExitSize %s Poll %s QueueRemain %s".
            //        formatted(count, config.exitSize, byteBuffer.toString(), queue.size())), true);

            if (byteBuffer == null) continue;

            if (isClose) return;

            IntBuffer intBuffer = BufferUtils.createIntBuffer(1);
            AL10.alGenBuffers(intBuffer);

            // 设定缓冲区数据
            AL10.alBufferData(
                    intBuffer.get(0),
                    channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16,
                    byteBuffer,
                    frequency
            );

            // 设定音量
            AL10.alSourcef(alSourceName, AL10.AL_GAIN, AllMusic.getVolume());

            AL10.alSourceQueueBuffers(alSourceName, intBuffer);

            // 如果没有播放，那么播放缓冲区！
            if (AL10.alGetSourcei(alSourceName, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING)
                AL10.alSourcePlay(alSourceName);
        }
    }

    public void closePlayer() {
        isClose = true;
    }

    public void setMusic(String url) {
        time = 0;
        closePlayer();
        urls.add(url);
        semaphore.release();
    }

    private void getClose() {
        if (get != null && !get.isAborted()) {
            get.abort();
            get = null;
        }
    }

    private void streamClose() throws IOException {
        if (content != null) {
            content.close();
            content = null;
        }
    }

    private void decodeClose() throws Exception {
        if (decoder != null) {
            decoder.close();
            decoder = null;
        }
    }

    @Override
    public int read() throws IOException {
        return content.read();
    }

    @Override
    public int read(byte[] buf) throws IOException {
        return content.read(buf);
    }

    @Override
    public synchronized int read(byte[] buf, int off, int len) throws IOException {
        try {
            int temp = content.read(buf, off, len);
            local += temp;
            return temp;
        } catch (ConnectionClosedException | SocketException ex) {
            connect();
            return read(buf, off, len);
        }
    }

    @Override
    public synchronized int available() throws IOException {
        return content.available();
    }

    @Override
    public void close() throws IOException {
        streamClose();
    }

    public void setLocal(long local) throws IOException {
        getClose();
        streamClose();
        this.local = local;
        connect();
    }

    public void setReload() {
        if (isPlay) {
            reload = true;
            isClose = true;
        }
    }
}
