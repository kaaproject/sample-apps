package org.kaaproject.kaa.demo.notification.fragment;

public interface OnFragmentUpdateEvent {
    /**
     * Send event to fragments, they must update their views because of new information
     */
    void onRefresh();
}