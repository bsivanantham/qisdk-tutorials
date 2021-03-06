/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.execute;

import android.os.Bundle;
import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionImportance;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionValidity;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.ConversationStatus;
import com.aldebaran.qi.sdk.object.conversation.QiChatExecutor;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The activity for the Execute tutorial.
 */
public class ExecuteTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "ExecuteTutorialActivity";
    private ConversationView conversationView;
    private ConversationBinder conversationBinder;
    private Chat chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationView = findViewById(R.id.conversationView);

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        // Bind the conversational events to the view.
        ConversationStatus conversationStatus = qiContext.getConversation().status(qiContext.getRobotContext());
        conversationBinder = conversationView.bindConversationTo(conversationStatus);

        // Create a topic.
        final Topic topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.execute)
                .build();

        // Create a qiChatbot
        final QiChatbot qiChatbot = QiChatbotBuilder.with(qiContext).withTopic(topic).build();
        Map<String, QiChatExecutor> executors = new HashMap<>();

        // Map the executor name from the topic to our qiChatbotExecutor
        executors.put("myExecutor", new MyQiChatExecutor(qiContext));

        // Set the executors to the qiChatbot
        qiChatbot.setExecutors(executors);

        // Build chat with the chatbots
        chat = ChatBuilder.with(qiContext).withChatbot(qiChatbot).build();
        chat.addOnStartedListener(() -> {
            //Say proposal to user
            Bookmark bookmark = topic.getBookmarks().get("execute_proposal");
            qiChatbot.goToBookmark(bookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
        });

        chat.async().run();
    }

    @Override
    public void onRobotFocusLost() {
        Log.i(TAG, "Focus lost.");

        if (conversationBinder != null) {
            conversationBinder.unbind();
        }

        // Remove the listeners from the chat.
        if (chat != null) {
            chat.removeAllOnStartedListeners();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Nothing here.
    }

    @Override
    protected int getLayoutId() {
        return R.layout.conversation_layout;
    }


    private void displayLine(final String text, final ConversationItemType type) {
        runOnUiThread(() -> conversationView.addLine(text, type));
    }

    class MyQiChatExecutor extends BaseQiChatExecutor {
        private final QiContext qiContext;

        MyQiChatExecutor(QiContext context) {
            super(context);
            this.qiContext = context;
        }

        @Override
        public void runWith(List<String> params) {
            // This is called when execute is reached in the topic
            animate(qiContext);
        }

        @Override
        public void stop() {
            // This is called when chat is canceled or stopped.
            displayLine("QiChatExecutor stopped", ConversationItemType.INFO_LOG);
            Log.i(TAG, "QiChatExecutor stopped");
        }


        private void animate(QiContext qiContext) {
            // Create an animation.
            Animation animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
                    .withResources(R.raw.raise_both_hands_b001) // Set the animation resource.
                    .build(); // Build the animation.

            // Create an animate action.
            Animate animate = AnimateBuilder.with(qiContext) // Create the builder with the context.
                    .withAnimation(animation) // Set the animation.
                    .build(); // Build the animate action.
            displayLine("Animation started.", ConversationItemType.INFO_LOG);
            animate.run();
            displayLine("Animation finished.", ConversationItemType.INFO_LOG);

        }
    }
}
