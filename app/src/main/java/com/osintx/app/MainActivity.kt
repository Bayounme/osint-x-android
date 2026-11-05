package com.osintx.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.widget.*
import android.text.InputType
import java.net.URLEncoder

class MainActivity : Activity() {

    private val bg = Color.rgb(8, 15, 30)
    private val panel = Color.rgb(17, 29, 49)
    private val cyan = Color.rgb(0, 220, 220)
    private val white = Color.WHITE
    private lateinit var searchInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createDashboard()
    }

    private fun createDashboard() {
        val scroll = ScrollView(this)
        scroll.setBackgroundColor(bg)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(22, 30, 22, 30)

        scroll.addView(layout)

        val logo = text("OSINT X", 32, cyan, true)
        layout.addView(logo)

        layout.addView(text(
            "MOBILE INTELLIGENCE TOOLKIT",
            11, Color.LTGRAY, true
        ))

        addSpace(layout, 28)

        layout.addView(text("Dashboard", 23, white, true))
        layout.addView(text(
            "Public information intelligence tools",
            13, Color.LTGRAY, false
        ))

        addSpace(layout, 22)

        val status = card()
        status.addView(text("SYSTEM STATUS", 11, cyan, true))
        status.addView(text(
            "●  OSINT X READY",
            18, white, true
        ))
        layout.addView(status)

        addSpace(layout, 25)

        layout.addView(text("Investigation Tools", 20, white, true))
        addSpace(layout, 12)

        addTool(layout, "🌐  DOMAIN LOOKUP",
            "Contoh: example.com")

        addTool(layout, "📡  IP LOOKUP",
            "Contoh: 8.8.8.8")

        addTool(layout, "👤  USERNAME SEARCH",
            "Masukkan username publik")

        addTool(layout, "✉️  EMAIL DOMAIN CHECK",
            "Masukkan alamat email")

        addSpace(layout, 20)

        layout.addView(text("Search Target", 18, white, true))
        addSpace(layout, 10)

        searchInput = EditText(this)
        searchInput.setSingleLine(true)
        searchInput.hint = "Masukkan target..."
        searchInput.setHintTextColor(Color.GRAY)
        searchInput.setTextColor(white)
        searchInput.setPadding(18, 14, 18, 14)
        searchInput.background = rounded(panel, 12)
        layout.addView(searchInput)

        addSpace(layout, 12)

        val searchButton = Button(this)
        searchButton.text = "SEARCH PUBLIC INFORMATION"
        searchButton.setTextColor(bg)
        searchButton.background = rounded(cyan, 12)
        layout.addView(searchButton)

        searchButton.setOnClickListener {
            performSearch()
        }

        addSpace(layout, 25)

        val notice = card()
        notice.addView(text("ETHICAL USE", 13, cyan, true))
        notice.addView(text(
            "Gunakan hanya informasi yang tersedia secara publik " +
            "atau yang kamu miliki izin untuk periksa. " +
            "Jangan gunakan untuk mengakses akun pribadi atau " +
            "mengambil data tanpa izin.",
            13, Color.LTGRAY, false
        ))
        layout.addView(notice)

        addSpace(layout, 25)

        layout.addView(text(
            "OSINT X  •  VERSION 1.0",
            11, Color.GRAY, true
        ))

        setContentView(scroll)
    }

    private fun addTool(
        parent: LinearLayout,
        title: String,
        hint: String
    ) {
        val box = card()
        val button = TextView(this)

        button.text = title
        button.textSize = 15f
        button.setTextColor(white)
        button.setTypeface(null, Typeface.BOLD)
        button.setPadding(5, 8, 5, 8)
        button.isClickable = true
        button.isFocusable = true

        button.setOnClickListener {
            searchInput.hint = hint
            searchInput.requestFocus()
        }

        box.addView(button)
        parent.addView(box)
        addSpace(parent, 9)
    }

    private fun performSearch() {
        val target = searchInput.text.toString().trim()

        if (target.isEmpty()) {
            Toast.makeText(
                this,
                "Masukkan target terlebih dahulu",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val query = when {
            target.contains("@") -> {
                val domain = target.substringAfter("@")
                "public information about domain $domain"
            }
            else -> "$target public information"
        }

        val encoded = URLEncoder.encode(query, "UTF-8")
        val url = "https://www.google.com/search?q=$encoded"

        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        )
    }

    private fun card(): LinearLayout {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(18, 17, 18, 17)
        box.background = rounded(panel, 14)
        return box
    }

    private fun text(
        value: String,
        size: Int,
        color: Int,
        bold: Boolean
    ): TextView {
        val view = TextView(this)
        view.text = value
        view.textSize = size.toFloat()
        view.setTextColor(color)

        if (bold) {
            view.setTypeface(null, Typeface.BOLD)
        }

        return view
    }

    private fun rounded(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }
    }

    private fun addSpace(parent: LinearLayout, height: Int) {
        val space = View(this)
        parent.addView(
            space,
            LinearLayout.LayoutParams(
                1,
                (height * resources.displayMetrics.density).toInt()
            )
        )
    }
}
