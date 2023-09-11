package com.jiale.gg_cam.listeners;


/**
 * Custom Button Listener
 * <p>
 * Custom Button Listener
 * </p>
 *
 * @author Jiale Dong
 * @version 1.0
 * @since 2023-09-10
 */
public class CustomButtonListener {
    public interface CustomEventListener {
        void onEvent();
    }
    private CustomEventListener listener;

    // 设置监听器
    public void setListener(CustomEventListener listener) {
        this.listener = listener;
    }

    // 触发事件
    public void triggerEvent() {
        if (listener != null) {
            listener.onEvent();
        }
    }
}
