package com.xiaxinyu.reactivex;

import com.xiaxinyu.reactivex.entity.Record;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ReactivexApplication.class})
public class ReactivexTest {
    @Autowired
    @Qualifier("asyncSendNoticeExecutor")
    Executor executor;

    private void doSendAndUpdateRecord(Record record) {
        log.info("I am ok");
        throw new RuntimeException("I am wrong");
    }


    @Test
    public void test() throws InterruptedException {
        Record record = Record.builder().maxRetryCount(1).build();

        Integer maxRetryCount = record.getMaxRetryCount();
        Integer factRetryCount = maxRetryCount - 1;

        if (maxRetryCount == 1) {
            doSendAndUpdateRecord(record);
        } else {
            Observable.just(record)
                    .map(t -> {
                        doSendAndUpdateRecord(record);
                        return t;
                    })
                    .retryWhen(x -> x.zipWith(
                            Observable.range(1, factRetryCount),
                            (e, retryCount) -> {
                                log.info("重新发送邮件： retryCount={}, businessType={}, templateId={}, receiveAccount={}",
                                        retryCount, record.getBusinessType(), record.getTemplateId(), record.getReceiveAccount());

                                if (retryCount >= factRetryCount) {
                                    log.warn("error.emailSend.retrySendError {}", e);
                                }
                                return retryCount + 1;
                            }).flatMap(y -> Observable.timer(1, TimeUnit.SECONDS)))
                    .subscribeOn(Schedulers.from(executor))
                    .subscribe((Record rc) -> {
                    });
        }


        Thread.sleep(5000);
    }

    @Test
    public void test1() {
        Observable.timer(1, TimeUnit.SECONDS)
                .doOnSubscribe(s -> System.out.println("subscribing"))
                .map(v -> {
                    throw new RuntimeException();
                })
                .retryWhen(errors -> {
                    AtomicInteger counter = new AtomicInteger();
                    return errors
                            .takeWhile(e -> counter.getAndIncrement() != 3)
                            .flatMap(e -> {
                                System.out.println("delay retry by " + counter.get() + " second(s)");
                                return Observable.timer(counter.get(), TimeUnit.SECONDS);
                            });
                })
                .blockingSubscribe(System.out::println, System.out::println);
    }
}
