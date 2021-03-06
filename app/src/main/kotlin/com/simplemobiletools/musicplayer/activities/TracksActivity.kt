package com.simplemobiletools.musicplayer.activities

import android.content.ContentUris
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.musicplayer.R
import com.simplemobiletools.musicplayer.adapters.SongsAdapter
import com.simplemobiletools.musicplayer.extensions.getTracks
import com.simplemobiletools.musicplayer.extensions.resetQueueItems
import com.simplemobiletools.musicplayer.helpers.ALBUM
import com.simplemobiletools.musicplayer.helpers.RESTART_PLAYER
import com.simplemobiletools.musicplayer.helpers.TRACK
import com.simplemobiletools.musicplayer.helpers.artworkUri
import com.simplemobiletools.musicplayer.models.Album
import com.simplemobiletools.musicplayer.models.AlbumHeader
import com.simplemobiletools.musicplayer.models.ListItem
import kotlinx.android.synthetic.main.activity_songs.*

// Artists -> Albums -> Songs
class TracksActivity : SimpleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_songs)

        val albumType = object : TypeToken<Album>() {}.type
        val album = Gson().fromJson<Album>(intent.getStringExtra(ALBUM), albumType)
        title = album.title

        getTracks(album.id) { tracks ->
            val items = ArrayList<ListItem>()
            val coverArt = ContentUris.withAppendedId(artworkUri, album.id.toLong()).toString()
            val header = AlbumHeader(album.title, coverArt, album.year, tracks.size, tracks.sumBy { it.duration }, album.artist)
            items.add(header)
            items.addAll(tracks)

            runOnUiThread {
                SongsAdapter(this, items, songs_list) {
                    resetQueueItems(tracks) {
                        Intent(this, TrackActivity::class.java).apply {
                            putExtra(TRACK, Gson().toJson(it))
                            putExtra(RESTART_PLAYER, true)
                            startActivity(this)
                        }
                    }
                }.apply {
                    songs_list.adapter = this
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }
}
