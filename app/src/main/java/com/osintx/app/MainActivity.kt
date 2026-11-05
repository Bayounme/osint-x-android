package com.osintx.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.View
import android.widget.*
import android.content.Intent
import android.net.Uri
import org.json.JSONObject
import java.net.URL
import java.net.HttpURLConnection
import java.net.URLEncoder

class MainActivity : Activity() {

    private lateinit var input: EditText
    private lateinit var output: TextView
    private lateinit var spinner: Spinner
    private lateinit var history: TextView

    private val tools = arrayOf(
        "Domain DNS",
        "IP Intelligence",
        "WHOIS / RDAP",
        "Email Security Check",
        "Username Search"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 28, 24, 24)
            setBackgroundColor(Color.rgb(10, 17, 32))
        }

        val title = TextView(this).apply {
            text = "OSINT X"
            textSize = 30f
            setTextColor(Color.rgb(0, 220, 220))
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "PUBLIC INFORMATION INTELLIGENCE"
            textSize = 11f
            setTextColor(Color.LTGRAY)
        }
        root.addView(subtitle)

        spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            tools
        )
        root.addView(spinner)

        input = EditText(this).apply {
            hint = "Masukkan domain, IP, email, atau username"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            setSingleLine(true)
        }
        root.addView(input)

        val searchButton = Button(this).apply {
            text = "MULAI ANALISIS"
            setOnClickListener { runAnalysis() }
        }
        root.addView(searchButton)

        val historyButton = Button(this).apply {
            text = "TAMPILKAN RIWAYAT"
            setOnClickListener { showHistory() }
        }
        root.addView(historyButton)

        output = TextView(this).apply {
            text = "Hasil analisis akan muncul di sini."
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(0, 20, 0, 20)
        }

        history = TextView(this).apply {
            textSize = 12f
            setTextColor(Color.LTGRAY)
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(output)
            addView(history)
        }

        val scroll = ScrollView(this)
        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun runAnalysis() {
        val query = input.text.toString().trim()

        if (query.isEmpty()) {
            input.error = "Masukkan data terlebih dahulu"
            return
        }

        saveHistory(query)
        output.text = "Sedang melakukan analisis..."

        Thread {
            val result = try {
                when (spinner.selectedItemPosition) {
                    0 -> domainLookup(query)
                    1 -> ipLookup(query)
                    2 -> rdapLookup(query)
                    3 -> emailCheck(query)
                    4 -> {
                        openUsernameSearch(query)
                        "Pencarian username publik dibuka di browser."
                    }
                    else -> "Fitur tidak tersedia."
                }
            } catch (e: Exception) {
                "Analisis gagal: ${e.message}"
            }

            runOnUiThread {
                output.text = result
            }
        }.start()
    }

    private fun request(url: String): JSONObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 12000
        connection.readTimeout = 12000
        connection.setRequestProperty("User-Agent", "OSINT-X-Android")

        try {
            val stream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val body = stream.bufferedReader().use { it.readText() }

            if (connection.responseCode !in 200..299) {
                throw Exception("Server merespons HTTP ${connection.responseCode}")
            }

            return JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun dns(domain: String, type: String): String {
        val encoded = URLEncoder.encode(domain, "UTF-8")
        val json = request(
            "https://dns.google/resolve?name=$encoded&type=$type"
        )

        val answers = json.optJSONArray("Answer")
            ?: return "Tidak ditemukan"

        val values = mutableListOf<String>()

        for (i in 0 until answers.length()) {
            val item = answers.getJSONObject(i)
            values.add(item.optString("data"))
        }

        return if (values.isEmpty()) "Tidak ditemukan"
        else values.joinToString("\n")
    }

    private fun domainLookup(domain: String): String {
        return """
            DOMAIN DNS ANALYSIS
            ==================

            Domain: $domain

            A RECORD:
            ${dns(domain, "A")}

            AAAA RECORD:
            ${dns(domain, "AAAA")}

            MX RECORD:
            ${dns(domain, "MX")}

            NS RECORD:
            ${dns(domain, "NS")}

            TXT RECORD:
            ${dns(domain, "TXT")}
        """.trimIndent()
    }

    private fun ipLookup(ip: String): String {
        val json = request("https://ipapi.co/$ip/json/")

        if (json.has("error") && json.optBoolean("error")) {
            return "IP tidak dapat diperiksa: ${json.optString("reason")}"
        }

        return """
            IP INTELLIGENCE
            ===============

            IP: ${json.optString("ip")}
            Negara: ${json.optString("country_name")}
            Wilayah: ${json.optString("region")}
            Kota: ${json.optString("city")}
            Kode Pos: ${json.optString("postal")}
            Koordinat: ${json.optString("latitude")}, ${json.optString("longitude")}
            ISP / Organisasi: ${json.optString("org")}
            ASN: ${json.optString("asn")}
            Zona Waktu: ${json.optString("timezone")}

            Catatan: lokasi IP hanya perkiraan,
            bukan lokasi perangkat secara langsung.
        """.trimIndent()
    }

    private fun rdapLookup(domain: String): String {
        val json = request("https://rdap.org/domain/$domain")

        val nameservers = json.optJSONArray("nameservers")
        val nsList = mutableListOf<String>()

        if (nameservers != null) {
            for (i in 0 until nameservers.length()) {
                nsList.add(
                    nameservers.getJSONObject(i)
                        .optString("ldhName")
                )
            }
        }

        return """
            DOMAIN RDAP INFORMATION
            ======================

            Domain: ${json.optString("ldhName")}
            Handle: ${json.optString("handle")}
            Status: ${json.optJSONArray("status")?.toString() ?: "Tidak tersedia"}

            Nameservers:
            ${if (nsList.isEmpty()) "Tidak tersedia" else nsList.joinToString("\n")}

            Sumber: RDAP publik
            Catatan: sebagian data pemilik domain
            mungkin disembunyikan oleh registrar.
        """.trimIndent()
    }

    private fun emailCheck(email: String): String {
        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email).matches()) {
            return "Format email tidak valid."
        }

        val domain = email.substringAfter("@")

        val mx = dns(domain, "MX")
        val txt = dns(domain, "TXT")
        val dmarc = dns("_dmarc.$domain", "TXT")

        val spfFound = txt.contains("v=spf1", true)
        val dmarcFound = dmarc.contains("v=DMARC1", true)

        return """
            EMAIL DOMAIN SECURITY
            =====================

            Email: $email
            Domain: $domain

            MX RECORD:
            $mx

            SPF:
            ${if (spfFound) "Ditemukan" else "Tidak ditemukan"}

            DMARC:
            ${if (dmarcFound) "Ditemukan" else "Tidak ditemukan"}

            TXT RECORD:
            $txt

            Catatan: pemeriksaan ini hanya memeriksa
            konfigurasi domain, bukan memastikan
            kotak email benar-benar aktif.
        """.trimIndent()
    }

    private fun openUsernameSearch(username: String) {
        val query = URLEncoder.encode(
            "\"$username\" profile",
            "UTF-8"
        )

        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/search?q=$query")
        )

        startActivity(intent)
    }

    private fun saveHistory(query: String) {
        val prefs = getSharedPreferences("osint_history", MODE_PRIVATE)
        val old = prefs.getString("items", "") ?: ""
        val updated = (listOf(query) + old.split("\n")
            .filter { it.isNotBlank() && it != query })
            .take(30)
            .joinToString("\n")

        prefs.edit().putString("items", updated).apply()
    }

    private fun showHistory() {
        val prefs = getSharedPreferences("osint_history", MODE_PRIVATE)
        val items = prefs.getString("items", "") ?: ""

        history.text = if (items.isBlank()) {
            "Belum ada riwayat pencarian."
        } else {
            "RIWAYAT PENCARIAN\n\n$items"
        }
    }
}
