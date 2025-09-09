package yk.plugin.layoutinspector.utils

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object MyCoroutines {
    val scope = CoroutineScope(CoroutineName("slayout-inspector") + Dispatchers.Default)
}