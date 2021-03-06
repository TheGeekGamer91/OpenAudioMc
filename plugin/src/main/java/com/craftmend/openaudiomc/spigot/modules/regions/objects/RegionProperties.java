package com.craftmend.openaudiomc.spigot.modules.regions.objects;

import com.craftmend.openaudiomc.spigot.OpenAudioMcSpigot;
import com.craftmend.openaudiomc.generic.media.objects.Media;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegionProperties {

    private String source;

    public Media getMedia() {
        return OpenAudioMcSpigot.getInstance().getRegionModule().getRegionMedia(source);
    }

}
