package com.droidoffice.slide.exception

import com.droidoffice.core.exception.DroidOfficeException

/**
 * Base exception for DroidSlide-specific errors.
 */
open class DroidSlideException(
    message: String,
    cause: Throwable? = null,
) : DroidOfficeException(message, cause)
