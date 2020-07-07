package io.github.centrifugal.centrifuge.example;

import io.github.centrifugal.centrifuge.Client;
import io.github.centrifugal.centrifuge.DuplicateSubscriptionException;
import io.github.centrifugal.centrifuge.PresenceStatsResult;
import io.github.centrifugal.centrifuge.RefreshEvent;
import io.github.centrifugal.centrifuge.ReplyCallback;
import io.github.centrifugal.centrifuge.JoinEvent;
import io.github.centrifugal.centrifuge.LeaveEvent;
import io.github.centrifugal.centrifuge.Options;
import io.github.centrifugal.centrifuge.EventListener;
import io.github.centrifugal.centrifuge.PublishResult;
import io.github.centrifugal.centrifuge.ReplyError;
import io.github.centrifugal.centrifuge.Subscription;
import io.github.centrifugal.centrifuge.SubscriptionEventListener;
import io.github.centrifugal.centrifuge.ConnectEvent;
import io.github.centrifugal.centrifuge.DisconnectEvent;
import io.github.centrifugal.centrifuge.ErrorEvent;
import io.github.centrifugal.centrifuge.MessageEvent;
import io.github.centrifugal.centrifuge.PrivateSubEvent;
import io.github.centrifugal.centrifuge.PublishEvent;
import io.github.centrifugal.centrifuge.SubscribeErrorEvent;
import io.github.centrifugal.centrifuge.SubscribeSuccessEvent;
import io.github.centrifugal.centrifuge.TokenCallback;
import io.github.centrifugal.centrifuge.UnsubscribeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
        LOGGER.info("Initialize Client client");
        SubscriptionEventListener subListener = new SubscriptionEventListener() {
            @Override
            public void onSubscribeSuccess(Subscription sub, SubscribeSuccessEvent event) {
                System.out.println("subscribed to " + sub.getChannel());
                String data="{\"input\": \"I just subscribed to channel\"}";
                sub.publish(data.getBytes(), new ReplyCallback<PublishResult>() {
                    @Override
                    public void onFailure(Throwable e) {
                        System.out.println("sub publish error: " + e);
                    }

                    @Override
                    public void onDone(ReplyError err, PublishResult res) {
                        if (err != null) {
                            System.out.println("error publish: " + err.getMessage());
                            return;
                        }
                        System.out.println("successfully published");
                    }
                });
            }
            @Override
            public void onSubscribeError(Subscription sub, SubscribeErrorEvent event) {
                System.out.println("subscribe error " + sub.getChannel() + " " + event.getMessage());
            }
            @Override
            public void onPublish(Subscription sub, PublishEvent event) {
                String data = new String(event.getData(), UTF_8);
                System.out.println("message from " + sub.getChannel() + " " + data);
            }
            @Override
            public void onUnsubscribe(Subscription sub, UnsubscribeEvent event) {
                System.out.println("unsubscribed " + sub.getChannel());
            }
            @Override
            public void onJoin(Subscription sub, JoinEvent event) {
                System.out.println("client " + event.getInfo().getClient() + " joined channel " + sub.getChannel());
            }
            @Override
            public void onLeave(Subscription sub, LeaveEvent event) {
                System.out.println("client " + event.getInfo().getClient() + " left channel " + sub.getChannel());
            }
        };
        EventListener listener = new EventListener() {
            @Override
            public void onConnect(Client client, ConnectEvent event) {
                System.out.println("connected");


            }

            @Override
            public void onDisconnect(Client client, DisconnectEvent event) {
                System.out.printf("disconnected %s, reconnect %s%n", event.getReason(), event.getReconnect());
            }

            @Override
            public void onError(Client client, ErrorEvent event) {
                System.out.println("There was a problem connecting!");
            }

            @Override
            public void onPrivateSub(Client client, PrivateSubEvent event, TokenCallback cb) {
                cb.Done("boom");
            }

            @Override
            public void onRefresh(Client client, RefreshEvent event, TokenCallback cb) {
                cb.Done("boom");
            }

            @Override
            public void onMessage(Client client, MessageEvent event) {
                String data = new String(event.getData(), UTF_8);
                System.out.println("message received: " + data);
            }
        };

        Client client = new Client(
                "ws://localhost:8080/connection/websocket?format=protobuf",
                new Options(),
                listener
        );
        //
        client.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoxLCJhcHBfaWQiOjAsImlhdCI6MTU5NDEwMzQ5MywiaXNzIjoib25saW5lLWdhbWUifQ.ipBkFXTvUqcyU5m9Na1cFdYFk_436ZFsprRG0o85qr8");
        String data="{\"input\": \"hi from Java\"}";
        client.setConnectData(data.getBytes());
        client.connect();
        Thread.sleep(50000);
        Subscription sub;
        try {
            sub = client.newSubscription("game:95", subListener);
        } catch (DuplicateSubscriptionException e) {
            e.printStackTrace();
            return;
        }
        sub.subscribe();

        Thread.sleep(20000000L);

//        String data="{\"input\": \"hi from Java\"}";
//
//        // Publish to channel (won't wait until client subscribed to channel).
//        // This message most probably won't be received by this client.
//        // You need to wait for subscription success to reliably receive
//        // publication - see publishing over sub object below.
//        client.publish("chat:index", data.getBytes(), new ReplyCallback<PublishResult>() {
//            @Override
//            public void onFailure(Throwable e) {
//                System.out.println("publish error: " + e);
//            }
//
//            @Override
//            public void onDone(ReplyError err, PublishResult reply) {
//                if (err != null) {
//                    System.out.println("error publish: " + err.getMessage());
//                    return;
//                }
//                System.out.println("successfully published");
//            }
//        });
//
//        // Publish via subscription (will wait for subscribe success before publishing).
//        sub.publish(data.getBytes(), new ReplyCallback<PublishResult>() {
//            @Override
//            public void onFailure(Throwable e) {
//                System.out.println("sub publish error: " + e);
//            }
//
//            @Override
//            public void onDone(ReplyError err, PublishResult res) {
//                if (err != null) {
//                    System.out.println("error publish: " + err.getMessage());
//                    return;
//                }
//                System.out.println("successfully published");
//            }
//        });
//
//        sub.presenceStats(new ReplyCallback<PresenceStatsResult>() {
//            @Override
//            public void onFailure(Throwable e) {
//                System.out.println("presence stats error: " + e);
//            }
//
//            @Override
//            public void onDone(ReplyError err, PresenceStatsResult res) {
//                if (err != null) {
//                    System.out.println("error presence stats: " + err.getMessage());
//                    return;
//                }
//                System.out.println("Num clients connected: " + res.getNumClients());
//            }
//        });
//
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException ex) {
//            Thread.currentThread().interrupt();
//        }
//
//        sub.unsubscribe();
//
//        // Say Client that we finished with Subscription. It will be removed from
//        // internal Subscription map so you can create new Subscription to channel
//        // later calling newSubscription method again.
//        client.removeSubscription(sub);
//
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException ex) {
//            Thread.currentThread().interrupt();
//        }
        // Disconnect from server.
        client.disconnect();
    }
}
