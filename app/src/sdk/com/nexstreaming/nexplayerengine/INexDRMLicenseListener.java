package com.nexstreaming.nexplayerengine;


/**
 * \brief The application must implement this interface in order to receive
 *         events from NexPlayer.
 */
public interface INexDRMLicenseListener {

    /**
     * \brief This method provides the license request data that will be used by DRM Client
     * @param requestData requestData is the license request data.
     * @return A \c Object with license response data. DRM Client will use this \c string without any modification.
     * 			If modification is not needed, then the UI should return null.
     */
    public byte[] onLicenseRequest(byte[] requestData);

}
