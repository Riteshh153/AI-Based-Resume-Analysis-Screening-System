package com.hiresmart.util;

import javafx.animation.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class AnimationUtils {

    public static void fadeIn(Node node, double durationMs) {
        if (node == null) return;
        node.setOpacity(0.0);
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    public static void slideInUp(Node node, double distanceY, double durationMs) {
        if (node == null) return;
        node.setOpacity(0.0);
        node.setTranslateY(distanceY);

        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);

        TranslateTransition tt = new TranslateTransition(Duration.millis(durationMs), node);
        tt.setFromY(distanceY);
        tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition pt = new ParallelTransition(node, ft, tt);
        pt.play();
    }

    public static void slideInRight(Node node, double distanceX, double durationMs) {
        if (node == null) return;
        node.setOpacity(0.0);
        node.setTranslateX(-distanceX);

        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);

        TranslateTransition tt = new TranslateTransition(Duration.millis(durationMs), node);
        tt.setFromX(-distanceX);
        tt.setToX(0);
        tt.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition pt = new ParallelTransition(node, ft, tt);
        pt.play();
    }

    public static void scaleIn(Node node, double durationMs) {
        if (node == null) return;
        node.setScaleX(0.85);
        node.setScaleY(0.85);
        node.setOpacity(0.0);

        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setToValue(1.0);

        ScaleTransition st = new ScaleTransition(Duration.millis(durationMs), node);
        st.setToX(1.0);
        st.setToY(1.0);
        st.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition pt = new ParallelTransition(node, ft, st);
        pt.play();
    }

    public static void pulse(Node node) {
        if (node == null) return;
        ScaleTransition st1 = new ScaleTransition(Duration.millis(120), node);
        st1.setToX(1.05);
        st1.setToY(1.05);

        ScaleTransition st2 = new ScaleTransition(Duration.millis(120), node);
        st2.setToX(1.0);
        st2.setToY(1.0);

        SequentialTransition seq = new SequentialTransition(st1, st2);
        seq.play();
    }

    public static void shake(Node node) {
        if (node == null) return;
        TranslateTransition tt = new TranslateTransition(Duration.millis(60), node);
        tt.setFromX(0);
        tt.setByX(8);
        tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }

    public static void animateCounter(Label label, int targetValue, double durationMs) {
        if (label == null) return;
        IntegerProperty count = new SimpleIntegerProperty(0);
        count.addListener((obs, oldVal, newVal) -> label.setText(String.format("%,d", newVal.intValue())));

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(count, 0)),
                new KeyFrame(Duration.millis(durationMs), new KeyValue(count, targetValue, Interpolator.EASE_OUT))
        );
        timeline.play();
    }
}
