package top.role.android.feature.market.detail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

import top.role.android.core.model.market.MobileMarketDetail
import top.role.android.core.model.region.MarketRegionSelection


@Composable
fun MarketPurchaseSection(
    listing:
    MobileMarketDetail,

    quantity:
    Double,

    selectedRegion:
    MarketRegionSelection?,

    receiverName:
    String,

    receiverPhone:
    String,

    addressLine1:
    String,

    postalCode:
    String,

    buyerNote:
    String,

    enabled:
    Boolean,

    onDecreaseQuantity:
        () -> Unit,

    onIncreaseQuantity:
        () -> Unit,

    onRegionClick:
        () -> Unit,

    onReceiverNameChange:
        (String) -> Unit,

    onReceiverPhoneChange:
        (String) -> Unit,

    onAddressLine1Change:
        (String) -> Unit,

    onPostalCodeChange:
        (String) -> Unit,

    onBuyerNoteChange:
        (String) -> Unit,
) {

    Column(
        modifier =
            Modifier.fillMaxWidth(),

        verticalArrangement =
            Arrangement.spacedBy(
                16.dp
            ),
    ) {

        /*
         * ------------------------------------------------
         * Title
         * ------------------------------------------------
         */

        Text(
            text =
                "购买信息",

            style =
                MaterialTheme
                    .typography
                    .titleMedium,

            fontWeight =
                FontWeight.Medium,
        )


        /*
         * ------------------------------------------------
         * Quantity
         * ------------------------------------------------
         */

        QuantityRow(
            listing =
                listing,

            quantity =
                quantity,

            enabled =
                enabled,

            onDecrease =
                onDecreaseQuantity,

            onIncrease =
                onIncreaseQuantity,
        )


        /*
         * ------------------------------------------------
         * Shipping
         * ------------------------------------------------
         */

        if (
            listing.needsShippingAddress
        ) {

            HorizontalDivider()


            Text(
                text =
                    "收货信息",

                style =
                    MaterialTheme
                        .typography
                        .titleSmall,

                fontWeight =
                    FontWeight.Medium,
            )


            OutlinedTextField(
                value =
                    receiverName,

                onValueChange =
                    onReceiverNameChange,

                modifier =
                    Modifier.fillMaxWidth(),

                enabled =
                    enabled,

                singleLine =
                    true,

                label = {
                    Text(
                        text =
                            "收货人"
                    )
                },

                placeholder = {
                    Text(
                        text =
                            "请输入收货人姓名"
                    )
                },
            )


            OutlinedTextField(
                value =
                    receiverPhone,

                onValueChange =
                    onReceiverPhoneChange,

                modifier =
                    Modifier.fillMaxWidth(),

                enabled =
                    enabled,

                singleLine =
                    true,

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Phone,
                    ),

                label = {
                    Text(
                        text =
                            "手机号"
                    )
                },

                placeholder = {
                    Text(
                        text =
                            "请输入联系电话"
                    )
                },
            )


            RegionField(
                selectedRegion =
                    selectedRegion,

                enabled =
                    enabled,

                onClick =
                    onRegionClick,
            )


            OutlinedTextField(
                value =
                    addressLine1,

                onValueChange =
                    onAddressLine1Change,

                modifier =
                    Modifier.fillMaxWidth(),

                enabled =
                    enabled,

                minLines =
                    2,

                maxLines =
                    4,

                label = {
                    Text(
                        text =
                            "详细地址"
                    )
                },

                placeholder = {
                    Text(
                        text =
                            "街道、门牌号、小区、楼栋和房间号"
                    )
                },
            )


            OutlinedTextField(
                value =
                    postalCode,

                onValueChange =
                    onPostalCodeChange,

                modifier =
                    Modifier.fillMaxWidth(),

                enabled =
                    enabled,

                singleLine =
                    true,

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Number,
                    ),

                label = {
                    Text(
                        text =
                            "邮政编码（选填）"
                    )
                },
            )
        }


        /*
         * ------------------------------------------------
         * Buyer note
         * ------------------------------------------------
         */

        HorizontalDivider()


        OutlinedTextField(
            value =
                buyerNote,

            onValueChange =
                onBuyerNoteChange,

            modifier =
                Modifier.fillMaxWidth(),

            enabled =
                enabled,

            minLines =
                2,

            maxLines =
                4,

            label = {
                Text(
                    text =
                        "买家备注（选填）"
                )
            },

            placeholder = {
                Text(
                    text =
                        "如有特殊要求，可以在这里告诉卖家"
                )
            },
        )
    }
}


@Composable
private fun QuantityRow(
    listing:
    MobileMarketDetail,

    quantity:
    Double,

    enabled:
    Boolean,

    onDecrease:
        () -> Unit,

    onIncrease:
        () -> Unit,
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.SpaceBetween,
    ) {

        Column(
            verticalArrangement =
                Arrangement.spacedBy(
                    2.dp
                ),
        ) {

            Text(
                text =
                    "购买数量",

                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,

                fontWeight =
                    FontWeight.Medium,
            )


            if (
                listing.stockMode !=
                "unlimited"
            ) {

                listing.stockCount
                    ?.let { stockCount ->

                        Text(
                            text =
                                "库存 ${stockCount.toInt()}",

                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant,
                        )
                    }
            }
        }


        if (
            listing.allowMultiQuantity
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    ),
            ) {

                OutlinedButton(
                    onClick =
                        onDecrease,

                    enabled =
                        enabled &&
                                quantity > 1.0,
                ) {
                    Text(
                        text =
                            "−"
                    )
                }


                Text(
                    text =
                        quantity
                            .toInt()
                            .toString(),

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.SemiBold,
                )


                val canIncrease =
                    listing.stockMode ==
                            "unlimited" ||
                            listing.stockCount ==
                            null ||
                            quantity <
                            listing.stockCount


                OutlinedButton(
                    onClick =
                        onIncrease,

                    enabled =
                        enabled &&
                                canIncrease,
                ) {
                    Text(
                        text =
                            "+"
                    )
                }
            }

        } else {

            Text(
                text =
                    "1",

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.SemiBold,
            )
        }
    }
}


@Composable
private fun RegionField(
    selectedRegion:
    MarketRegionSelection?,

    enabled:
    Boolean,

    onClick:
        () -> Unit,
) {

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    enabled =
                        enabled,
                ) {
                    onClick()
                }
                .padding(
                    vertical =
                        4.dp,
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                8.dp
            ),
    ) {

        Text(
            text =
                "所在地区",

            style =
                MaterialTheme
                    .typography
                    .labelMedium,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
        )


        Row(
            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.SpaceBetween,
        ) {

            Text(
                text =
                    selectedRegion
                        ?.regionText
                        ?: "请选择省 / 市 / 区县",

                modifier =
                    Modifier.weight(
                        1f
                    ),

                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,

                color =
                    if (
                        selectedRegion !=
                        null
                    ) {
                        MaterialTheme
                            .colorScheme
                            .onSurface
                    } else {
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                    },
            )


            Text(
                text =
                    "›",

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
            )
        }


        HorizontalDivider()
    }
}