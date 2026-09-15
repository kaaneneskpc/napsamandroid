package com.kaaneneskpc.napsam.core.data.di

import javax.inject.Qualifier

/** Disk ve ağ işleri (spesifikasyon B6.3: dispatcher'lar DI ile gelir). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/** CPU işleri (JSON çözme, motor). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

/** Uygulama ömrü boyunca süren işler (ör. DataStore). `GlobalScope` yerine. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
