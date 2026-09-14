package com.kaaneneskpc.napsam

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// onCreate içinde ağ, disk veya SDK başlatma yapılmaz (spesifikasyon B11).
@HiltAndroidApp
class NapsamApplication : Application()
