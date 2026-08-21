package top.role.android.feature.market.detail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import top.role.android.core.model.region.ChinaRegionNode
import top.role.android.core.model.region.MarketRegionSelection


private enum class RegionPickerLevel {
    Province,
    City,
    District,
}


@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun MarketRegionPicker(
    regions:
    List<ChinaRegionNode>,

    onDismiss:
        () -> Unit,

    onSelected:
        (MarketRegionSelection) -> Unit,
) {

    var level by
    remember {
        mutableStateOf(
            RegionPickerLevel.Province
        )
    }

    var selectedProvince by
    remember {
        mutableStateOf<
                ChinaRegionNode?
                >(
            null
        )
    }

    var selectedCity by
    remember {
        mutableStateOf<
                ChinaRegionNode?
                >(
            null
        )
    }


    val currentItems =
        when (
            level
        ) {
            RegionPickerLevel.Province ->
                regions

            RegionPickerLevel.City ->
                selectedProvince
                    ?.children
                    .orEmpty()

            RegionPickerLevel.District ->
                selectedCity
                    ?.children
                    .orEmpty()
        }


    ModalBottomSheet(
        onDismissRequest =
            onDismiss,
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        bottom = 24.dp,
                    ),
        ) {

            RegionPickerHeader(
                level =
                    level,

                province =
                    selectedProvince,

                city =
                    selectedCity,

                onBack = {
                    when (
                        level
                    ) {
                        RegionPickerLevel.Province -> {
                            onDismiss()
                        }

                        RegionPickerLevel.City -> {
                            selectedProvince =
                                null

                            level =
                                RegionPickerLevel.Province
                        }

                        RegionPickerLevel.District -> {
                            selectedCity =
                                null

                            level =
                                RegionPickerLevel.City
                        }
                    }
                },
            )


            HorizontalDivider()


            LazyColumn {

                items(
                    items =
                        currentItems,

                    key = {
                        it.value
                    },
                ) { item ->

                    RegionPickerItem(
                        item =
                            item,

                        onClick = {

                            when (
                                level
                            ) {

                                RegionPickerLevel.Province -> {

                                    selectedProvince =
                                        item

                                    /*
                                     * 某些数据如果省下面直接没有 city，
                                     * 就不继续进入空列表。
                                     */
                                    if (
                                        item.children
                                            .isEmpty()
                                    ) {
                                        return@RegionPickerItem
                                    }

                                    level =
                                        RegionPickerLevel.City
                                }


                                RegionPickerLevel.City -> {

                                    selectedCity =
                                        item

                                    if (
                                        item.children
                                            .isEmpty()
                                    ) {
                                        /*
                                         * 极少数特殊地区如果没有 district，
                                         * 暂时允许 city 自己充当最终一级。
                                         */
                                        val province =
                                            selectedProvince
                                                ?: return@RegionPickerItem

                                        onSelected(
                                            MarketRegionSelection(
                                                provinceName =
                                                    province.label,

                                                provinceCode =
                                                    province.value,

                                                cityName =
                                                    item.label,

                                                cityCode =
                                                    item.value,

                                                districtName =
                                                    "",

                                                districtCode =
                                                    "",
                                            )
                                        )

                                        onDismiss()

                                        return@RegionPickerItem
                                    }

                                    level =
                                        RegionPickerLevel.District
                                }


                                RegionPickerLevel.District -> {

                                    val province =
                                        selectedProvince
                                            ?: return@RegionPickerItem

                                    val city =
                                        selectedCity
                                            ?: return@RegionPickerItem

                                    onSelected(
                                        MarketRegionSelection(
                                            provinceName =
                                                province.label,

                                            provinceCode =
                                                province.value,

                                            cityName =
                                                city.label,

                                            cityCode =
                                                city.value,

                                            districtName =
                                                item.label,

                                            districtCode =
                                                item.value,
                                        )
                                    )

                                    onDismiss()
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}


@Composable
private fun RegionPickerHeader(
    level:
    RegionPickerLevel,

    province:
    ChinaRegionNode?,

    city:
    ChinaRegionNode?,

    onBack:
        () -> Unit,
) {

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 12.dp,
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                8.dp
            ),
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween,
        ) {

            Text(
                text =
                    "‹",

                modifier =
                    Modifier.clickable {
                        onBack()
                    },

                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
            )

            Text(
                text =
                    "选择所在地区",

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.SemiBold,
            )

            /*
             * 占位，保证标题视觉居中。
             */
            Text(
                text =
                    "  ",
            )
        }


        val path =
            when (
                level
            ) {
                RegionPickerLevel.Province ->
                    "请选择省份"

                RegionPickerLevel.City ->
                    province
                        ?.label
                        ?: "请选择城市"

                RegionPickerLevel.District ->
                    listOfNotNull(
                        province
                            ?.label,

                        city
                            ?.label,
                    )
                        .joinToString(
                            " "
                        )
            }

        Text(
            text =
                path,

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


@Composable
private fun RegionPickerItem(
    item:
    ChinaRegionNode,

    onClick:
        () -> Unit,
) {

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
                .padding(
                    horizontal = 20.dp,
                    vertical = 15.dp,
                ),
    ) {

        Text(
            text =
                item.label,

            style =
                MaterialTheme
                    .typography
                    .bodyLarge,
        )
    }
}