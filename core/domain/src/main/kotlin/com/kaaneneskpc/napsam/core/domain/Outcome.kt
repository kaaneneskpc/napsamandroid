package com.kaaneneskpc.napsam.core.domain

/**
 * Veri katmanı sonucu (spesifikasyon B3.8). İstisnalar istemci katmanına sızmaz; ağ hataları burada
 * tiplenir ve arayüz her birinde yerel öneriye düşer.
 */
public sealed interface Outcome<out T> {
    public data class Success<out T>(public val value: T) : Outcome<T>

    public data class Failure(public val error: DataError) : Outcome<Nothing>
}

public enum class DataError {
    /** Bağlantı yok veya kesildi. */
    NETWORK,

    /** İstek süresi aşıldı ("Yaz" modunda 5 sn). */
    TIMEOUT,

    /** 429: Vercel güvenlik duvarı hız sınırı. */
    RATE_LIMITED,

    /** 5xx. */
    SERVER,

    /** 4xx veya beklenmeyen yanıt biçimi. */
    INVALID_RESPONSE,
}
