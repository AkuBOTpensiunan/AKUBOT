package com.example.data.seed

import com.example.data.local.QuestionEntity

object StarterQuestionBank {
    const val DEFAULT_DOCUMENT_NAME = "Bank_Soal_Ritel_Beanspot_Bawaan.csv"
    const val DEFAULT_DOCUMENT_FORMAT = "BAWAAN"

    fun createInitialQuestions(documentId: Long, now: Long): List<QuestionEntity> = listOf(
        QuestionEntity(
            questionText = "Bagus adalah crew toko Alfamart, di toko alfamart tempat Kurniawan bekerja tidak hanya menjual kebutuhan pokok seperti beras, minyak dan juga makanan ringan, tetapi tersedia juga area kopi siap saji dan makanan hangat. Bagus masuk dalam jenis toko beanspot concept?",
            optionsText = "Advance; Basic; Medium; Premium; RRI",
            answerKey = "Medium",
            explanation = "Konsep Bean Spot Medium menggabungkan area minimarket kebutuhan pokok reguler dengan konter kopi siap saji serta makanan hangat standar.",
            category = "BEANSPOT",
            sourceDocumentId = documentId,
            createdAt = now,
            updatedAt = now
        ),
        QuestionEntity(
            questionText = "Suhu ideal penyimpanan biji kopi (coffee beans) pada gerai Bean Spot agar aroma dan kualitas tetap terjaga adalah?",
            optionsText = "0°C - 5°C; 15°C - 25°C; 30°C - 35°C; Di bawah 0°C; Di atas 40°C",
            answerKey = "15°C - 25°C",
            explanation = "Biji kopi harus disimpan di ruang sejuk, kering, dan kedap udara pada suhu ruang terkendali 15°C - 25°C serta terhindar dari sinar matahari langsung.",
            category = "BEANSPOT",
            sourceDocumentId = documentId,
            createdAt = now,
            updatedAt = now
        ),
        QuestionEntity(
            questionText = "Metode pengeluaran barang yang memprioritaskan produk dengan tanggal kedaluwarsa lebih dekat untuk dijual terlebih dahulu disebut?",
            optionsText = "FIFO; LIFO; FEFO; Average; JIT",
            answerKey = "FEFO",
            explanation = "FEFO (First Expired First Out) memastikan produk dengan masa kedaluwarsa terdekat keluar atau terjual terlebih dahulu.",
            category = "INVENTORY",
            sourceDocumentId = documentId,
            createdAt = now,
            updatedAt = now
        ),
        QuestionEntity(
            questionText = "Apa perbedaan utama antara metode FIFO dan FEFO dalam penataan produk di rak toko?",
            optionsText = "FIFO berdasar harga, FEFO berdasar warna; FIFO berdasar tanggal masuk, FEFO berdasar tanggal kedaluwarsa; FIFO khusus makanan basah, FEFO khusus elektronik; Keduanya sama persis; FEFO berdasar urutan abjad",
            answerKey = "FIFO berdasar tanggal masuk, FEFO berdasar tanggal kedaluwarsa",
            explanation = "FIFO (First In First Out) mengutamakan barang yang datang lebih awal, sedangkan FEFO mengutamakan tanggal kedaluwarsa terdekat.",
            category = "INVENTORY",
            sourceDocumentId = documentId,
            createdAt = now,
            updatedAt = now
        ),
        QuestionEntity(
            questionText = "Saat melayani transaksi di kasir, langkah standar menyapa pelanggan yang benar sesuai SOP pelayanan adalah?",
            optionsText = "Langsung scan barang tanpa bicara; Senyum, salam, sapa dan tawarkan bantuan; Menanyakan uang pas terlebih dahulu; Meminta struk lama; Menunggu pelanggan menyapa duluan",
            answerKey = "Senyum, salam, sapa dan tawarkan bantuan",
            explanation = "Standar pelayanan kasir 3S (Senyum, Salam, Sapa) dilakukan dengan kontak mata ramah sebelum memindai produk.",
            category = "KASIR",
            sourceDocumentId = documentId,
            createdAt = now,
            updatedAt = now
        ),
        QuestionEntity(
            questionText = "Jika terjadi selisih harga antara label price tag di rak dengan harga yang muncul di sistem POS kasir, tindakan yang tepat adalah?",
            optionsText = "Memaksa pelanggan membayar harga tertinggi; Memberikan harga terendah sesuai price tag rak dan segera lapor ke pimpinan toko; Membatalkan semua belanjaan; Mengubah harga secara manual tanpa konfirmasi; Mengabaikan komplain pelanggan",
            answerKey = "Memberikan harga terendah sesuai price tag rak dan segera lapor ke pimpinan toko",
            explanation = "Pelanggan berhak mendapatkan harga yang tertera pada rak apabila update label belum dilakukan, kemudian tim toko wajib segera memperbarui price tag.",
            category = "KASIR",
            sourceDocumentId = documentId,
            createdAt = now,
            updatedAt = now
        ),
        QuestionEntity(
            questionText = "Kegiatan merapikan, memajukan produk ke bagian depan rak (facing out), dan memastikan label harga menghadap ke depan disebut?",
            optionsText = "Stock Opname; Planogram; Facing Up; Receiving; Retur",
            answerKey = "Facing Up",
            explanation = "Facing up / display depan bertujuan agar rak terlihat penuh, rapi, dan mudah dijangkau oleh konsumen.",
            category = "MERCHANDISING",
            sourceDocumentId = documentId,
            createdAt = now,
            updatedAt = now
        ),
        QuestionEntity(
            questionText = "Dokumen panduan visual yang mengatur tata letak, posisi rak, dan jumlah facings setiap SKU produk di toko disebut?",
            optionsText = "Faktur Pajak; Planogram; Surat Jalan; Berita Acara; Kartu Stok",
            answerKey = "Planogram",
            explanation = "Planogram adalah diagram atau peta penataan produk di gondola maupun chiller agar sesuai standar merchandising.",
            category = "MERCHANDISING",
            sourceDocumentId = documentId,
            createdAt = now,
            updatedAt = now
        ),
        QuestionEntity(
            questionText = "Minuman kopi susu khas Bean Spot yang memadukan espresso, susu segar, dan gula aren dikenal dengan nama?",
            optionsText = "Americano; Kopi Susu Gula Aren; Caramel Macchiato; Matcha Latte; Cold Brew",
            answerKey = "Kopi Susu Gula Aren",
            explanation = "Menu signature kopi siap saji di Bean Spot yang paling populer adalah Kopi Susu Gula Aren.",
            category = "BEANSPOT",
            sourceDocumentId = documentId,
            createdAt = now,
            updatedAt = now
        ),
        QuestionEntity(
            questionText = "Pada konsep toko Bean Spot Premium, fasilitas tambahan apa yang umumnya tersedia bagi pelanggan dibandingkan tipe Basic?",
            optionsText = "Hanya menjual air mineral botol; Tersedia mesin espresso komersial, bakery hangat, dan area duduk nyaman; Tidak memiliki mesin kopi; Hanya melayani transaksi grosir; Tutup di malam hari",
            answerKey = "Tersedia mesin espresso komersial, bakery hangat, dan area duduk nyaman",
            explanation = "Bean Spot tipe Premium hadir dengan konsep cafe-minimarket lengkap meliputi seating area, fresh bakery, dan minuman espresso crafted.",
            category = "BEANSPOT",
            sourceDocumentId = documentId,
            createdAt = now,
            updatedAt = now
        ),
        QuestionEntity(
            questionText = "Dalam prosedur penerimaan barang dari DC (Distribution Center), hal pertama yang wajib diperiksa sebelum membongkar muatan adalah?",
            optionsText = "Membuka semua segel box langsung; Memeriksa kecocokan nomor segel kontainer dan surat jalan; Langsung memajang barang ke rak; Menginput retur; Menghitung uang kasir",
            answerKey = "Memeriksa kecocokan nomor segel kontainer dan surat jalan",
            explanation = "Validasi nomor segel fisik dengan dokumen surat jalan memastikan keamanan dan keutuhan pengiriman dari gudang pusat.",
            category = "SOP TOKO",
            sourceDocumentId = documentId,
            createdAt = now,
            updatedAt = now
        ),
        QuestionEntity(
            questionText = "Berapa suhu standar chiller untuk penyimpanan produk susu pasteurisasi dan minuman siap saji agar tetap aman dikonsumsi?",
            optionsText = "0°C - 4°C; 10°C - 15°C; 18°C - 22°C; -18°C; 25°C - 30°C",
            answerKey = "0°C - 4°C",
            explanation = "Chiller minuman dan produk dairy segar dijaga pada rentang 0°C hingga 4°C untuk menghambat pertumbuhan bakteri.",
            category = "SOP TOKO",
            sourceDocumentId = documentId,
            createdAt = now,
            updatedAt = now
        ),
        QuestionEntity(
            questionText = "Apa yang dimaksud dengan Stock Opname (SO) dalam operasional toko ritel?",
            optionsText = "Penghitungan fisik persediaan barang di toko untuk dicocokkan dengan data sistem; Promosi diskon akhir pekan; Pembersihan area gudang; Pendaftaran member baru; Pengiriman barang antar toko",
            answerKey = "Penghitungan fisik persediaan barang di toko untuk dicocokkan dengan data sistem",
            explanation = "Stock Opname memverifikasi akurasi stok fisik terhadap catatan komputer dan mendeteksi selisih atau kehilangan barang (shrinkage).",
            category = "INVENTORY",
            sourceDocumentId = documentId,
            createdAt = now,
            updatedAt = now
        ),
        QuestionEntity(
            questionText = "Istilah selisih negatif atau kehilangan stok barang di toko ritel akibat kerusakan, salah input, maupun pencurian disebut?",
            optionsText = "Margin; Shrinkage; Markup; Turnover; Rebate",
            answerKey = "Shrinkage",
            explanation = "Shrinkage mencerminkan penyusutan inventaris antara stok tercatat pada sistem dengan stok fisik aktual.",
            category = "INVENTORY",
            sourceDocumentId = documentId,
            createdAt = now,
            updatedAt = now
        ),
        QuestionEntity(
            questionText = "Saat pelanggan ingin melakukan pembayaran menggunakan QRIS di kasir, langkah verifikasi terpenting sebelum menyerahkan struk adalah?",
            optionsText = "Memastikan notifikasi status pembayaran Berhasil/Sukses muncul di layar POS atau EDC; Mempercayai tangkapan layar tanpa cek sistem; Langsung menutup laci kasir; Meminta PIN pelanggan; Mencatat nomor HP saja",
            answerKey = "Memastikan notifikasi status pembayaran Berhasil/Sukses muncul di layar POS atau EDC",
            explanation = "Verifikasi status sukses di terminal EDC/POS mencegah risiko bukti transfer palsu atau transaksi tertunda.",
            category = "KASIR",
            sourceDocumentId = documentId,
            createdAt = now,
            updatedAt = now
        ),
        QuestionEntity(
            questionText = "Produk makanan ringan dan permen yang diletakkan tepat di depan meja kasir bertujuan untuk mendorong?",
            optionsText = "Stock opname cepat; Impulse buying; Retur barang; Pembelian grosir; Penurunan stok gudang",
            answerKey = "Impulse buying",
            explanation = "Area kasir merupakan hot zone untuk impulse buying (pembelian tidak terencana) saat pelanggan mengantre.",
            category = "MERCHANDISING",
            sourceDocumentId = documentId,
            createdAt = now,
            updatedAt = now
        ),
        QuestionEntity(
            questionText = "Pada konter Bean Spot, alat yang digunakan untuk menghaluskan biji kopi sebelum proses ekstraksi espresso disebut?",
            optionsText = "Tamper; Coffee Grinder; Frother; Portafilter; Knock Box",
            answerKey = "Coffee Grinder",
            explanation = "Coffee grinder menggiling biji kopi sesuai tingkat kehalusan (grind size) yang dibutuhkan metode seduh.",
            category = "BEANSPOT",
            sourceDocumentId = documentId,
            createdAt = now,
            updatedAt = now
        ),
        QuestionEntity(
            questionText = "Proses pemadatan bubuk kopi di dalam portafilter menggunakan alat tekan datar sebelum dipasang ke mesin espresso disebut?",
            optionsText = "Brewing; Tamping; Steaming; Roasting; Cupping",
            answerKey = "Tamping",
            explanation = "Tamping meratakan dan memadatkan bubuk kopi agar tekanan air panas saat ekstraksi mengalir merata.",
            category = "BEANSPOT",
            sourceDocumentId = documentId,
            createdAt = now,
            updatedAt = now
        ),
        QuestionEntity(
            questionText = "Dalam kebersihan toko dan area Bean Spot, kode warna lap mikrofiber yang berbeda untuk meja pelanggan dan area peralatan kopi bertujuan untuk?",
            optionsText = "Estetika toko; Mencegah kontaminasi silang; Menghemat deterjen; Mempermudah pencucian di rumah; Membedakan shift karyawan",
            answerKey = "Mencegah kontaminasi silang",
            explanation = "Pemisahan lap berdasarkan zona kerja (food contact vs non-food contact) adalah standar wajib keamanan pangan.",
            category = "SOP TOKO",
            sourceDocumentId = documentId,
            createdAt = now,
            updatedAt = now
        ),
        QuestionEntity(
            questionText = "Ketika melakukan penawaran produk promosi kasir (tebus murah atau PSM), sikap terbaik yang harus ditunjukkan crew toko adalah?",
            optionsText = "Menawarkan dengan sopan dan menyebutkan keuntungan hematnya tanpa memaksa; Memasukkan barang ke keranjang tanpa izin; Tidak pernah menawarkan promosi; Mengabaikan pelanggan reguler; Memberikan diskon tanpa scan",
            answerKey = "Menawarkan dengan sopan dan menyebutkan keuntungan hematnya tanpa memaksa",
            explanation = "Penawaran aktif yang ramah membantu pelanggan mengetahui promo hemat sekaligus meningkatkan basket size secara positif.",
            category = "KASIR",
            sourceDocumentId = documentId,
            createdAt = now,
            updatedAt = now
        )
    )
}
