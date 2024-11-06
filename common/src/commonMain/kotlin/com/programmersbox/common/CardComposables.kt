package com.programmersbox.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.programmersbox.common.generated.resources.Res
import com.programmersbox.common.generated.resources.font_awesome_stuff
import org.jetbrains.compose.resources.Font

@Composable
fun rememberDeck(vararg key: Any) = remember(key) { Deck.defaultDeck() }

object PlayingCardDefaults {
    val shape = RoundedCornerShape(7.dp)
}

data class CardShow(
    val full: (Card) -> String = { it.toSymbolString() },
    val suit: (Suit) -> String = { it.unicodeSymbol },
)

val LocalCardShowing = staticCompositionLocalOf<CardShow> { CardShow() }

@Composable
fun PlayingCard(
    card: Card,
    modifier: Modifier = Modifier,
    tonalElevation: Dp = 4.dp,
    shape: Shape = PlayingCardDefaults.shape,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(color),
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: () -> Unit = {},
) = Surface(
    onClick = onClick,
    shape = shape,
    tonalElevation = tonalElevation,
    enabled = enabled,
    contentColor = contentColor,
    shadowElevation = shadowElevation,
    border = border,
    interactionSource = interactionSource,
    modifier = modifier//.size(100.dp, 150.dp),
) {
    CardDetail(card)
}

@Composable
fun PlayingCard(
    card: Card,
    useNewDesign: Boolean,
    modifier: Modifier = Modifier,
    tonalElevation: Dp = 4.dp,
    shape: Shape = PlayingCardDefaults.shape,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = contentColorFor(color),
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    showFullDetail: Boolean = true,
) = Surface(
    shape = shape,
    tonalElevation = tonalElevation,
    contentColor = contentColor,
    shadowElevation = shadowElevation,
    border = border,
    modifier = modifier//.size(100.dp, 150.dp),
) {
    if (useNewDesign) CardDetailNew(card, showFullDetail)
    else CardDetail(card, showFullDetail)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardDetail(
    card: Card,
    showFullDetail: Boolean = true,
) {
    val colors = LocalCardColor.current
    val textColor = when (card.color) {
        CardColor.Black -> colors.black
        CardColor.Red -> colors.red
    }
    val cardShow = LocalCardShowing.current
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = card.let(cardShow.full),
            color = textColor,
            textAlign = TextAlign.Start,
            modifier = Modifier.align(Alignment.Start),
        )

        Text(
            text = card.suit.let(cardShow.suit),
            color = textColor,
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardDetailNew(
    card: Card,
    showFullDetail: Boolean = true,
) {
    val cardShow = LocalCardShowing.current
    val ff = FontFamily(Font(resource = Res.font.font_awesome_stuff))
    val textColor = when (card.color) {
        CardColor.Black -> if (isSystemInDarkTheme()) Color.White else Color.Black
        CardColor.Red -> Color.Red
    }

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text(
                text = card.symbol + " ",
                color = textColor,
                textAlign = TextAlign.Start
            )
            Text(
                text = card.suit.unicodeSymbol,
                color = textColor,
                fontFamily = ff,
                textAlign = TextAlign.Start
            )
        }
        Text(
            text = when (card.value) {
                13 -> "♚"
                12 -> "♛"
                11 -> "⚔"
                else -> card.suit.let(cardShow.suit)
            },
            color = textColor,
            fontFamily = ff,
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmptyCard(
    modifier: Modifier = Modifier,
    tonalElevation: Dp = 4.dp,
    shape: Shape = PlayingCardDefaults.shape,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(color),
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    cardBack: Modifier? = null,
    content: @Composable () -> Unit = {},
    onClick: () -> Unit = {},
) = Surface(
    onClick = onClick,
    shape = shape,
    tonalElevation = tonalElevation,
    enabled = enabled,
    color = color,
    contentColor = contentColor,
    shadowElevation = shadowElevation,
    border = border,
    interactionSource = interactionSource,
    modifier = modifier//.size(100.dp, 150.dp),
) {
    cardBack?.let {
        Box(modifier = it) { content() }
    } ?: Box { content() }
}

@Composable
fun EmptyCard(
    modifier: Modifier = Modifier,
    tonalElevation: Dp = 4.dp,
    shape: Shape = PlayingCardDefaults.shape,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(color),
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    content: @Composable () -> Unit = {},
    cardBack: Modifier? = null,
) = Surface(
    shape = shape,
    tonalElevation = tonalElevation,
    contentColor = contentColor,
    color = color,
    shadowElevation = shadowElevation,
    border = border,
    modifier = modifier//.size(100.dp, 150.dp),
) {
    cardBack?.let {
        Box(modifier = it) { content() }
    } ?: Box { content() }
}
