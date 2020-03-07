package net.gazeplay;

import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class Polly {

    private static final String OUTPUT_FORMAT_MP3 = OutputFormat.Mp3.toString();
    private static final long SYNTHESIS_TASK_POLL_INTERVAL = 500;
    private static final long SYNTHESIS_TASK_POLL_DELAY = 1000;
    private static final int SYNTHESIS_TASK_TIMEOUT_SECONDS = 30;

    private AmazonPolly client;
    private String outputBucket;

    public Polly(String outputBucket) {
        this.client = AmazonPollyClientBuilder.defaultClient();
        this.outputBucket = outputBucket;
    }

    public List<Voice> listVoices() {
        DescribeVoicesRequest request = new DescribeVoicesRequest();
        DescribeVoicesResult result = client.describeVoices(request);
        return result.getVoices();
    }

    public void synthesizeSpeech(String message, String name, int voiceIndex) {
        StartSpeechSynthesisTaskRequest request = new StartSpeechSynthesisTaskRequest()
                .withOutputFormat(OUTPUT_FORMAT_MP3)
                .withText(message)
                .withTextType(TextType.Text)
                .withVoiceId(listVoices().get(voiceIndex).getId())
                .withOutputS3KeyPrefix(name)
                .withOutputS3BucketName(outputBucket);

        StartSpeechSynthesisTaskResult result = client.startSpeechSynthesisTask(request);
        String taskId = result.getSynthesisTask().getTaskId();

        await().with()
                .pollInterval(SYNTHESIS_TASK_POLL_INTERVAL, TimeUnit.MILLISECONDS)
                .pollDelay(SYNTHESIS_TASK_POLL_DELAY, TimeUnit.MILLISECONDS)
                .atMost(SYNTHESIS_TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .until(
                        () -> getSynthesisTaskStatus(taskId).equals(TaskStatus.Completed.toString())
                );
    }

    private String getSynthesisTaskStatus(String taskId) {
        GetSpeechSynthesisTaskRequest getSpeechSynthesisTaskRequest = new GetSpeechSynthesisTaskRequest()
                .withTaskId(taskId);
        GetSpeechSynthesisTaskResult result = client.getSpeechSynthesisTask(getSpeechSynthesisTaskRequest);
        return result.getSynthesisTask().getTaskStatus();
    }
}
