package com.example.nav3recipes.bottomsheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.example.nav3recipes.bottomsheet.BottomSheetSceneStrategy.Companion.bottomSheet
import kotlinx.coroutines.launch

val LocalBottomSheetDismiss = compositionLocalOf<() -> Unit> {
    error("No dismiss handler provided")
}


/** An [OverlayScene] that renders an [entry] within a [ModalBottomSheet]. */
@OptIn(ExperimentalMaterial3Api::class)
internal data class BottomSheetScene<T : Any>(
    override val key: T,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
    private val modalBottomSheetProperties: ModalBottomSheetProperties,
    private val onBack: () -> Unit,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)

    override val content: @Composable (() -> Unit) = {

        val lifecycleOwner = rememberLifecycleOwner()
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        val scope = rememberCoroutineScope()

        val animateDismiss: () -> Unit = {
            scope.launch {
                sheetState.hide()
            }.invokeOnCompletion {
                onBack()
            }
        }

        ModalBottomSheet(
            onDismissRequest = onBack,
            sheetState = sheetState,
            properties = modalBottomSheetProperties,
        ) {
            CompositionLocalProvider(
                LocalLifecycleOwner provides lifecycleOwner,
                LocalBottomSheetDismiss provides animateDismiss
            ) {
                entry.Content()
            }
        }
    }
}

/**
 * A [SceneStrategy] that displays entries that have added [bottomSheet] to their [NavEntry.metadata]
 * within a [ModalBottomSheet] instance.
 *
 * This strategy should always be added before any non-overlay scene strategies.
 */
@OptIn(ExperimentalMaterial3Api::class)
class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.lastOrNull() ?: return null
        val bottomSheetProperties = lastEntry.metadata[BottomSheetKey] ?: return null
        return bottomSheetProperties.let { properties ->
            @Suppress("UNCHECKED_CAST")
            BottomSheetScene(
                key = lastEntry.contentKey as T,
                previousEntries = entries.dropLast(1),
                overlaidEntries = entries.dropLast(1),
                entry = lastEntry,
                modalBottomSheetProperties = properties,
                onBack = onBack
            )
        }
    }

    companion object {
        fun bottomSheet(modalBottomSheetProperties: ModalBottomSheetProperties = ModalBottomSheetProperties()) =
            metadata {
                put(BottomSheetKey, modalBottomSheetProperties)
            }

        object BottomSheetKey : NavMetadataKey<ModalBottomSheetProperties>
    }
}