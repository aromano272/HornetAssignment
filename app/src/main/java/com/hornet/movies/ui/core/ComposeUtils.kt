package com.hornet.movies.ui.core

import android.graphics.Typeface
import android.text.Spanned
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

object Spacing {
    val dp4 = 4.dp
    val dp8 = 8.dp
    val dp16 = 16.dp
    val dp24 = 24.dp
    val dp32 = 32.dp
    val dp40 = 40.dp
    val dp48 = 48.dp
    val dp64 = 64.dp
}

@Composable
fun Spacer4() = Spacer(modifier = Modifier.size(Spacing.dp4))
@Composable
fun Spacer8() = Spacer(modifier = Modifier.size(Spacing.dp8))
@Composable
fun Spacer16() = Spacer(modifier = Modifier.size(Spacing.dp16))
@Composable
fun Spacer24() = Spacer(modifier = Modifier.size(Spacing.dp24))
@Composable
fun Spacer32() = Spacer(modifier = Modifier.size(Spacing.dp32))
@Composable
fun Spacer40() = Spacer(modifier = Modifier.size(Spacing.dp40))
@Composable
fun Spacer48() = Spacer(modifier = Modifier.size(Spacing.dp48))
@Composable
fun Spacer64() = Spacer(modifier = Modifier.size(Spacing.dp64))

@Composable
fun ColumnScope.SpacerWeight() = Spacer(modifier = Modifier.weight(1f))
@Composable
fun RowScope.SpacerWeight() = Spacer(modifier = Modifier.weight(1f))

@Composable
fun RowScope.LoadingButtonIcon() {
    CircularProgressIndicator(
        modifier = Modifier
            .then(Modifier.size(Spacing.dp24))
            .align(Alignment.CenterVertically),
        color = MaterialTheme.colorScheme.onPrimary,
        strokeWidth = 2.dp
    )
}

@Composable
fun RowScope.SecondaryLoadingButtonIcon() {
    CircularProgressIndicator(
        modifier = Modifier
            .then(Modifier.size(Spacing.dp24))
            .align(Alignment.CenterVertically),
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = 2.dp
    )
}

@Composable
fun composableIf(
    bool: Boolean,
    content: @Composable () -> Unit,
): (@Composable () -> Unit)? = if (bool) {
    content
} else null

@Composable
fun DrawableIcon(
    @DrawableRes id: Int,
    tint: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
) {
    Icon(
        modifier = modifier,
        painter = painterResource(id = id),
        contentDescription = null,
        tint = tint
    )
}

@Composable
fun LoadingButton(modifier: Modifier = Modifier) {
    Button(modifier = modifier, onClick = {}) {
        LoadingButtonIcon()
    }
}

@Composable
fun SecondaryLoadingButton(modifier: Modifier = Modifier) {
    OutlinedButton(modifier = modifier, onClick = {}) {
        SecondaryLoadingButtonIcon()
    }
}

@Composable
fun LoadingIconButton(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier.then(Modifier.size(Spacing.dp16)),
        color = Color.White,
        strokeWidth = 2.dp
    )
}

@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier,
    text: String,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        colors = colors,
        enabled = enabled,
        onClick = onClick,
    ) {
        Text(text)
    }
}

@Composable
fun SecondaryButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    OutlinedButton(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick,
    ) {
        Text(text)
    }
}

@Composable
fun LabelText(
    modifier: Modifier = Modifier,
    text: String,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        modifier = modifier,
        text = text,
        fontWeight = FontWeight.Bold,
        overflow = overflow
    )
}

@Composable
fun TextFieldWithLabel(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    placeholder: String? = null,
    onValueChange: (String) -> Unit,
    maxLines: Int = Int.MAX_VALUE,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    colors: TextFieldColors = TextFieldDefaults.colors(),
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label)

        Spacer24()

        TextField(
            value = value,
            placeholder = placeholder?.let { { Text(it) } },
            maxLines = maxLines,
            onValueChange = onValueChange,
            enabled = enabled,
            keyboardOptions = keyboardOptions,
            colors = colors,
        )
    }
}

@Composable
fun TableRow(
    modifier: Modifier = Modifier,
    label: String,
    value: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        LabelText(text = label)
        value()
    }
}

@Composable
fun TableRow(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueAlignStart: Boolean = false,
) {
    TableRow(
        label = label,
        value = {
            Text(
                modifier = Modifier.align(
                    if (valueAlignStart) Alignment.Start
                    else Alignment.End
                ),
                text = value,
                textAlign = if (valueAlignStart) TextAlign.Start else TextAlign.End
            )
        },
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun TableRow(
    modifier: Modifier = Modifier,
    label: String,
    value: AnnotatedString,
    valueAlignStart: Boolean = false,
) {
    TableRow(
        label = label,
        value = {
            Text(
                modifier = Modifier.align(
                    if (valueAlignStart) Alignment.Start
                    else Alignment.End
                ),
                text = value,
                textAlign = if (valueAlignStart) TextAlign.Start else TextAlign.End
            )
        },
        modifier = modifier.fillMaxWidth(),
    )
}

/**
 * Converts a [Spanned] into an [AnnotatedString] trying to keep as much formatting as possible.
 *
 * Currently supports `bold`, `italic`, `underline` and `color`.
 */
fun Spanned.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
    val spanned = this@toAnnotatedString
    append(spanned.toString())
    getSpans(0, spanned.length, Any::class.java).forEach { span ->
        val start = getSpanStart(span)
        val end = getSpanEnd(span)
        when (span) {
            is StyleSpan -> when (span.style) {
                Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                Typeface.BOLD_ITALIC -> addStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    ), start, end
                )
            }

            is UnderlineSpan -> addStyle(
                SpanStyle(textDecoration = TextDecoration.Underline),
                start,
                end
            )

            is ForegroundColorSpan -> addStyle(
                SpanStyle(color = Color(span.foregroundColor)),
                start,
                end
            )
        }
    }
}

@Composable
fun annotatedStringResource(@StringRes id: Int): AnnotatedString {
    val resources = LocalContext.current.resources
    val spanned = resources.getText(id) as SpannedString
    return spanned.toAnnotatedString()
}
