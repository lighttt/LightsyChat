package np.com.manishtuladhar.lightsychat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService{


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //getting title and body from the firebase function
        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_message = remoteMessage.getNotification().getBody();

        //getting result when notification is clicked
        String click_action = remoteMessage.getNotification().getClickAction();
        String from_user_id = remoteMessage.getData().get("from_user_id");

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notification_title)
                .setContentText(notification_message);


        //what happens when the notification is clicked
        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("user_id",from_user_id);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                                                        this,
                                                        0,
                                                        resultIntent,
                                                        PendingIntent.FLAG_UPDATE_CURRENT
                                                );

        mBuilder.setContentIntent(resultPendingIntent);

        //Sets an random ID for the notification
        int mNotificationID = (int)System.currentTimeMillis();
        //Gets an instance of Notification Manager service
        NotificationManager mNotifyMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        //Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationID,mBuilder.build());


    }
}
