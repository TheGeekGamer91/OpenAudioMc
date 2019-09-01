package com.craftmend.openaudiomc.generic.cards.objects;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.networking.client.objects.ClientConnection;
import com.craftmend.openaudiomc.generic.networking.packets.PacketClientCreateCard;
import com.craftmend.openaudiomc.generic.networking.packets.PacketClientDestroyCard;
import com.craftmend.openaudiomc.generic.networking.packets.PacketClientUpdateCard;
import com.google.gson.Gson;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Card {

    @Getter private String title;
    private List<Row> rows = new ArrayList<>();
    private UUID cardId = UUID.randomUUID();
    private transient List<String> ids = new ArrayList<>();

    public Card(String title) {
        this.title = title;
    }

    public Card updateText(String id, Text newText) {
        if (!ids.contains(id)) throw new IllegalArgumentException("There is no element called " + id + " in this card");

        this.rows.forEach(row -> {
            List<ReplacementText> replacementTexts = new ArrayList<>();
            for (Text text : row.getTextList()) {
                if (text.getId().equals(id)) {
                    int index = row.getTextList().indexOf(text);
                    replacementTexts.add(new ReplacementText(index, text));
                }
            }
            replacementTexts.forEach(replacementText -> {
                row.getTextList().set(replacementText.getIndex(), newText);
            });
        });

        PacketClientUpdateCard cardUpdate = new PacketClientUpdateCard(id, new Gson().toJson(newText));

        OpenAudioMc.getInstance().getNetworkingService().getClients().forEach(client -> {
            if (client.getCardId() != null && cardId.toString().equals(client.getCardId().toString())) {
                // update card
                OpenAudioMc.getInstance().getNetworkingService().send(client, cardUpdate);
            }
        });

        return this;
    }

    public Card addPlayer(Player player) {
        addPlayer(player.getUniqueId());
        return this;
    }

    public Card removePlayer(Player player) {
        removePlayer(player.getUniqueId());
        return this;
    }

    public Card addPlayer(UUID uuid) {
        ClientConnection clientConnection = OpenAudioMc.getApi().getClient(uuid);
        if (clientConnection == null) throw new IllegalStateException("Player does not have a client connection session");

        clientConnection.setCardId(cardId);
        // send show packet
        OpenAudioMc.getInstance().getNetworkingService().send(clientConnection, new PacketClientCreateCard(this
        ));
        return this;
    }

    public Card removePlayer(UUID uuid) {
        ClientConnection clientConnection = OpenAudioMc.getApi().getClient(uuid);
        if (clientConnection == null) throw new IllegalStateException("Player does not have a client connection session");

        if (clientConnection.getCardId() != null && clientConnection.getCardId().toString().equals(cardId.toString())) {
            clientConnection.setCardId(null);
        }

        // send destroy card packet
        OpenAudioMc.getInstance().getNetworkingService().send(clientConnection, new PacketClientDestroyCard());
        return this;
    }

    public Card addRow(Text... texts) {
        for (Text text : texts) {
            if (ids.contains(text.getId())) {
                throw new IllegalStateException("There already exists a element with id " + text.getId() + " in this card.");
            } else {
                ids.add(text.getId());
            }
        }

        if (texts.length > 15) throw new IllegalArgumentException("A row may not have more than 15 text elements");

        this.rows.add(new Row(Arrays.asList(texts)));
        return this;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }


}