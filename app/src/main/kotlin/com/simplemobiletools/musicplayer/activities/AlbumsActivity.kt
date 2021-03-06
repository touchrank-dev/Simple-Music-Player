package com.simplemobiletools.musicplayer.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.commons.extensions.getFormattedDuration
import com.simplemobiletools.musicplayer.R
import com.simplemobiletools.musicplayer.adapters.AlbumsAdapter
import com.simplemobiletools.musicplayer.extensions.getAlbums
import com.simplemobiletools.musicplayer.extensions.getTracksSync
import com.simplemobiletools.musicplayer.extensions.resetQueueItems
import com.simplemobiletools.musicplayer.helpers.ALBUM
import com.simplemobiletools.musicplayer.helpers.ARTIST
import com.simplemobiletools.musicplayer.helpers.RESTART_PLAYER
import com.simplemobiletools.musicplayer.helpers.TRACK
import com.simplemobiletools.musicplayer.models.*
import kotlinx.android.synthetic.main.activity_albums.*

// Artists -> Albums -> Tracks
class AlbumsActivity : SimpleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_albums)

        val artistType = object : TypeToken<Artist>() {}.type
        val artist = Gson().fromJson<Artist>(intent.getStringExtra(ARTIST), artistType)
        title = artist.title

        getAlbums(artist) { albums ->
            val listItems = ArrayList<ListItem>()
            val albumsSectionLabel = resources.getQuantityString(R.plurals.albums, albums.size, albums.size)
            listItems.add(AlbumSection(albumsSectionLabel))
            listItems.addAll(albums)

            var trackFullDuration = 0
            val tracksToAdd = ArrayList<Track>()
            albums.forEach {
                val tracks = getTracksSync(it.id)
                trackFullDuration += tracks.sumBy { it.duration }
                tracksToAdd.addAll(tracks)
            }

            var tracksSectionLabel = resources.getQuantityString(R.plurals.tracks, tracksToAdd.size, tracksToAdd.size)
            tracksSectionLabel += " • ${trackFullDuration.getFormattedDuration(true)}"
            listItems.add(AlbumSection(tracksSectionLabel))
            listItems.addAll(tracksToAdd)

            runOnUiThread {
                AlbumsAdapter(this, listItems, albums_list) {
                    if (it is Album) {
                        Intent(this, TracksActivity::class.java).apply {
                            putExtra(ALBUM, Gson().toJson(it))
                            startActivity(this)
                        }
                    } else {
                        resetQueueItems(tracksToAdd) {
                            Intent(this, TrackActivity::class.java).apply {
                                putExtra(TRACK, Gson().toJson(it))
                                putExtra(RESTART_PLAYER, true)
                                startActivity(this)
                            }
                        }
                    }
                }.apply {
                    albums_list.adapter = this
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }
}
