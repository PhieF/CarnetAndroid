/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.spisoft.quicknote.widget

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.text.Html
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.spisoft.quicknote.MainActivity
import com.spisoft.quicknote.Note
import com.spisoft.quicknote.R
import com.spisoft.quicknote.databases.NoteManager
import java.text.SimpleDateFormat
import java.util.*

class ListRemoteViewsFactory(app: Application, intent: Intent) : RemoteViewsService.RemoteViewsFactory {
    private val WIDTH = 80
    private val HEIGHT = 80
    private val app = app
    private val appWidgetId: Int
    private var notes: List<Note>? = null


    override fun onCreate() {
        notes = NoteManager.getNotes(app.applicationContext)
    }

    override fun onDataSetChanged() {
        notes = NoteManager.getNotes(app.applicationContext)
    }

    override fun onDestroy() {
    }

    override fun getCount(): Int {
        return notes!!.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val note: Note = notes!![position]
        val noteContent = NoteManager.getNoteContent(note, app.applicationContext)
        val noteText = Html.fromHtml(noteContent.get("html").toString())
        val noteMetadataJSON = noteContent.getJSONObject("metadata")
        val noteCreationDate: Long = noteMetadataJSON.get("creation_date") as Long
        val noteLastModificationDate: Long = noteMetadataJSON.get("last_modification_date") as Long

        var date = "";

        if (noteCreationDate.compareTo(-1) != 0) {
            date = SimpleDateFormat("dd/MM/yyyy").format(Date(noteCreationDate))
        }

        if(noteLastModificationDate.compareTo(-1) != 0) {
            date = SimpleDateFormat("dd/MM/yyyy").format(Date(noteLastModificationDate))
        }

        val intentDetail = Intent(app.applicationContext, MainActivity::class.java)
        intentDetail.action = Companion.ACTION_OPEN_NOTE
        intentDetail.putExtra("note_path", note.path)

        val row = RemoteViews(app.packageName, R.layout.note_layout_widget)
        row.setOnClickFillInIntent(R.id.root, intentDetail)

        row.setTextViewText(R.id.note_content, noteText)
        row.setTextViewText(R.id.note_title, note.title)
        row.setTextViewText(R.id.note_date, date)

        return row
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    init {
        appWidgetId = intent
                .getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    }

    companion object {
        private const val ACTION_OPEN_NOTE = "open_note"
    }
}