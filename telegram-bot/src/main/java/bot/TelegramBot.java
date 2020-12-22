package bot;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.enums.ModelObjectType;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.special.SearchResult;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.player.GetCurrentUsersRecentlyPlayedTracksRequest;
import com.wrapper.spotify.requests.data.playlists.GetListOfUsersPlaylistsRequest;
import com.wrapper.spotify.requests.data.search.SearchItemRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    private static final String clientId = "5d68dc795c8f4b138341afa848dddffe";
    private static final String clientSecret = "7872f86638ce4ddd9110375a259475a2";

    //private static final String track_url = "https://api.spotify.com/v1/users/tracks";
    //private static final String type = ModelObjectType.TRACK.name();
    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            //.setProxyUrl(track_url)
            //.setAccessToken("BQDaJlsFfC8epxBP10Qpu88yXNtihP44O3A9jojx6EzsvT3osZz4NOE46Y7xDvNLtuAmXLm_81IuGHkLKNpGOcqz7NkU_APnUo_8hHBgYalOMlgoxK7QGUsXufomjUprgarOlh1tsIpvDh5gmTsnWB-Ou6TIKZuIjQ")
            //.setRefreshToken("AQA2_gYKgR627eofiEM4L3gCBKhtRMjksJTEsizFCD53SBGs6RG29kLATb7aCn8V40D7aBParcuiXXeOtl8Pkv0jYjEIkMxWU5jThCQ9TLrnJER4iH56B6dfHN_8j5v5Wvc")
            .build();
    private static final ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials()
            .build();



    @SneakyThrows
    public synchronized void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {

//            String trackName = update.getMessage().getText();



            long chatId = update.getMessage().getChatId();
            String userId = update.getMessage().getText();
            String searchText = update.getMessage().getText();


            if (searchText.matches("[A-Za-z0-9_]")){


                try {
                    // For all requests an access token is needed
                    String id = update.getMessage().getText();
                    final ClientCredentials clientCredentials = clientCredentialsRequest.execute();

                    spotifyApi.setAccessToken(clientCredentials.getAccessToken());
                    // Set access token for further "spotifyApi" object usage
                    //ObjectMapper mapper = new ObjectMapper();
                    //Map<String, String> map = mapper.readValue(trackName, Map.class);

                    //SearchTracksRequest searchTracksRequest = spotifyApi.searchTracks(searchText).build();
                    //SearchItemRequest searchItemRequest = spotifyApi.searchItem(trackName, type).build();
                    GetTrackRequest getTrackRequest = spotifyApi.getTrack(id).build();
                    //SearchResult searchResult = searchItemRequest.execute();
                    //Paging<Track> tracks = searchTracksRequest.execute();
                    Track track = getTrackRequest.execute();



                    SendMessage message = new SendMessage()
                            .setChatId(chatId)
//                            .setText("Found " + tracks.getTotal() + " tracks for " + searchText)
                            .setText("Your track " + track.getName());
                    execute(message);

                    } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (SpotifyWebApiException e) {
                    e.printStackTrace();
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            }
            else if (searchText.equals("/listened")){

                try {
                    SendMessage message = new SendMessage()
                            .setChatId(chatId)
                            .setText("Listened songs");
                    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

                    List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();
                    KeyboardRow row = new KeyboardRow();
                    row.add("Останні прослухані пісні");
                    keyboard.add(row);
                    keyboardMarkup.setKeyboard(keyboard);
                    message.setReplyMarkup(keyboardMarkup);
                        execute(message);
                    } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            else if (searchText.equals("Останні прослухані пісні")) {
                final ClientCredentials clientCredentials = clientCredentialsRequest.execute();
                spotifyApi.setAccessToken(clientCredentials.getAccessToken());

                GetCurrentUsersRecentlyPlayedTracksRequest getCurrentUsersRecentlyPlayedTracksRequest = spotifyApi.getCurrentUsersRecentlyPlayedTracks().build();
                PagingCursorbased<PlayHistory> playHistoryPagingCursorbased = getCurrentUsersRecentlyPlayedTracksRequest.execute();
                SendMessage message = new SendMessage()
                        .setChatId(chatId)
                        .setText("Your recently played tracks: " + playHistoryPagingCursorbased.getTotal());
                execute(message);
            }
            else if (searchText.equals("/playlists")) {
                SendMessage message = new SendMessage()
                        .setChatId(chatId)
                        .setText("Введіть назву плейлисту");
                final ClientCredentials clientCredentials = clientCredentialsRequest.execute();
                spotifyApi.setAccessToken(clientCredentials.getAccessToken());
                GetListOfUsersPlaylistsRequest getListOfUsersPlaylistsRequest = spotifyApi.getListOfUsersPlaylists(userId).build();
                Paging<PlaylistSimplified> playlistSimplifiedPaging = getListOfUsersPlaylistsRequest.execute();



                message.setText("Your playlists: " + playlistSimplifiedPaging.getTotal());
                execute(message);
            }
        }
    }

    public String getBotUsername() {
        return "music_bot";
    }

    public String getBotToken() {
        return "1276165749:AAFV-am-ST3PNl3xJ-Ci5s1fvKo7nDEAJuU";
    }
}

