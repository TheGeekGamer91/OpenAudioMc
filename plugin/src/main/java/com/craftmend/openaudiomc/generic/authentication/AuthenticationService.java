package com.craftmend.openaudiomc.generic.authentication;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.loggin.OpenAudioLogger;
import com.craftmend.openaudiomc.generic.storage.enums.StorageLocation;
import com.craftmend.openaudiomc.generic.interfaces.OAConfiguration;
import com.craftmend.openaudiomc.generic.storage.enums.StorageKey;
import com.craftmend.openaudiomc.generic.authentication.objects.Key;
import com.craftmend.openaudiomc.generic.authentication.objects.ServerKeySet;

import com.craftmend.openaudiomc.generic.networking.rest.RestRequest;
import lombok.Getter;

import java.io.IOException;

public class AuthenticationService {

    @Getter private ServerKeySet serverKeySet = new ServerKeySet();
    @Getter private boolean isSuccesfull = false;
    @Getter private String failureMessage = "Oh no, it looks like the initial setup of OpenAudioMc has failed. Please try to restart the server and try again, if that still does not work, please contact OpenAudioMc staff or support.";
    private final int keyVersion = 2;

    public AuthenticationService() throws IllegalStateException {
        OpenAudioLogger.toConsole("Starting authentication module");
        loadData();

        // if (!isSuccesfull) throw new IllegalStateException("Failed to parse tokens");
    }

    /**
     * version of the authentication version that's currently stored
     * @return version
     */
    public int getAuthVersion() {
        int version = OpenAudioMc.getInstance().getOAConfiguration().getInt(StorageKey.AUTH_KEY_VERSION);
        return version == -1 ? 1 : version;
    }

    /**
     * Load the tokens from files.
     * If they dont exist, then they will be requested by the cool OpenAuioMc api.
     */
    private void loadData() {
        OAConfiguration spigotConfigurationModule = OpenAudioMc.getInstance().getOAConfiguration();

        if (spigotConfigurationModule.getString(StorageKey.AUTH_PRIVATE_KEY).equals("not-set") || getAuthVersion() != keyVersion) {
            //setup process
            try {
                new RestRequest("/register.php").execute().thenAccept((genericApiResponse) -> {
                    if (genericApiResponse.getErrors().size() == 0) {
                        serverKeySet.setPrivateKey(new Key(genericApiResponse.getData().get(0).getPrivateKey()));
                        serverKeySet.setPublicKey(new Key(genericApiResponse.getData().get(0).getPublicKey()));
                        spigotConfigurationModule.setString(StorageKey.AUTH_PRIVATE_KEY, serverKeySet.getPrivateKey().getValue());
                        spigotConfigurationModule.setString(StorageKey.AUTH_PUBLIC_KEY, serverKeySet.getPublicKey().getValue());
                        spigotConfigurationModule.setInt(StorageLocation.DATA_FILE, StorageKey.AUTH_KEY_VERSION.getPath(), keyVersion);
                        isSuccesfull = true;
                    } else {
                        OpenAudioLogger.toConsole("Failed to request token.");
                        isSuccesfull = false;
                    }
                });
            } catch (IOException e) {
                OpenAudioLogger.toConsole("Failed to request token.");
                isSuccesfull = false;
                e.printStackTrace();
            }
        } else {
            serverKeySet.setPrivateKey(new Key(spigotConfigurationModule.getString(StorageKey.AUTH_PRIVATE_KEY)));
            serverKeySet.setPublicKey(new Key(spigotConfigurationModule.getString(StorageKey.AUTH_PUBLIC_KEY)));
            isSuccesfull = true;
        }
    }
}
