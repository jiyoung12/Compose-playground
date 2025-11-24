#!/bin/bash

# 사용법: ./create_module.sh [type] [name]
# 예시: ./create_module.sh feature bar-chart
# 예시: ./create_module.sh core model

TYPE=$1
NAME=$2
# [변경] 요청하신 ID로 변경 완료
BASE_PACKAGE="com.jyhong.playground"

# 1. 입력값 검증
if [ -z "$TYPE" ] || [ -z "$NAME" ]; then
  echo "❌  사용법 오류: 모듈 타입과 이름을 입력해주세요."
  echo "👉  Usage: ./create_module.sh [core|feature] [module_name]"
  exit 1
fi

if [[ "$TYPE" != "core" && "$TYPE" != "feature" ]]; then
  echo "❌  타입 오류: 'core' 또는 'feature' 만 가능합니다."
  exit 1
fi

# 경로 및 패키지명 설정
# bar-chart -> bar_chart (패키지명용 언더바 변환)
NORMALIZED_NAME="${NAME//-/_}"
MODULE_PATH="$TYPE/$NAME"
PACKAGE_NAME="$BASE_PACKAGE.$TYPE.$NORMALIZED_NAME"
SRC_DIR="$MODULE_PATH/src/main/java/${PACKAGE_NAME//.//}"

echo "🚀  모듈 생성 시작: :$TYPE:$NAME"
echo "📦  Package: $PACKAGE_NAME"

# 2. 디렉토리 구조 생성
mkdir -p "$SRC_DIR"
mkdir -p "$MODULE_PATH/src/main/res"

# 3. .gitignore 생성
echo "/build" > "$MODULE_PATH/.gitignore"

# 4. build.gradle.kts 생성
GRADLE_FILE="$MODULE_PATH/build.gradle.kts"

echo "plugins {" > "$GRADLE_FILE"
echo "    id(\"jyhong.android.library\")" >> "$GRADLE_FILE"

if [ "$TYPE" == "feature" ]; then
    echo "    id(\"jyhong.android.library.compose\")" >> "$GRADLE_FILE"
    echo "    id(\"jyhong.android.hilt\")" >> "$GRADLE_FILE"
elif [ "$TYPE" == "core" ]; then
    if [ "$NAME" == "chart-engine" ] || [ "$NAME" == "designsystem" ] || [ "$NAME" == "ui" ]; then
         echo "    id(\"jyhong.android.library.compose\")" >> "$GRADLE_FILE"
    fi
    echo "    id(\"jyhong.android.hilt\")" >> "$GRADLE_FILE"
fi

echo "}" >> "$GRADLE_FILE"
echo "" >> "$GRADLE_FILE"
echo "android {" >> "$GRADLE_FILE"
echo "    namespace = \"$PACKAGE_NAME\"" >> "$GRADLE_FILE"
echo "}" >> "$GRADLE_FILE"
echo "" >> "$GRADLE_FILE"
echo "dependencies {" >> "$GRADLE_FILE"

if [ "$TYPE" == "feature" ]; then
    echo "    implementation(project(\":core:model\"))" >> "$GRADLE_FILE"
    echo "    implementation(project(\":core:domain\"))" >> "$GRADLE_FILE"
    echo "    implementation(project(\":core:designsystem\"))" >> "$GRADLE_FILE"
elif [ "$NAME" == "domain" ]; then
    echo "    implementation(project(\":core:model\"))" >> "$GRADLE_FILE"
elif [ "$NAME" == "data" ]; then
    echo "    implementation(project(\":core:model\"))" >> "$GRADLE_FILE"
    echo "    implementation(project(\":core:domain\"))" >> "$GRADLE_FILE"
fi

echo "}" >> "$GRADLE_FILE"

# 5. AndroidManifest.xml 생성
MANIFEST_FILE="$MODULE_PATH/src/main/AndroidManifest.xml"
echo "<manifest package=\"$PACKAGE_NAME\" />" > "$MANIFEST_FILE"

# 6. [NEW] Feature 모듈일 경우 Screen & Navigation 자동 생성
if [ "$TYPE" == "feature" ]; then
    # bar-chart -> BarChart (PascalCase 변환)
    PASCAL_NAME=$(echo "$NAME" | awk -F- '{for(i=1;i<=NF;i++) $i=toupper(substr($i,1,1)) substr($i,2)} 1' OFS="")
    
    # 6-1. Screen.kt 생성
    SCREEN_FILE="$SRC_DIR/${PASCAL_NAME}Screen.kt"
    echo "Creating Screen: $SCREEN_FILE"
    
    cat <<EOF > "$SCREEN_FILE"
package $PACKAGE_NAME

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ${PASCAL_NAME}Screen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$PASCAL_NAME Screen")
    }
}
EOF

    # 6-2. Navigation.kt 생성 (Route 변수 포함)
    NAV_FILE="$SRC_DIR/${PASCAL_NAME}Navigation.kt"
    ROUTE_NAME="${NORMALIZED_NAME}_route"
    
    echo "Creating Navigation: $NAV_FILE"
    
    cat <<EOF > "$NAV_FILE"
package $PACKAGE_NAME

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val ${NORMALIZED_NAME^^}_ROUTE = "$ROUTE_NAME"

fun NavController.navigateTo${PASCAL_NAME}(navOptions: NavOptions? = null) {
    this.navigate(${NORMALIZED_NAME^^}_ROUTE, navOptions)
}

fun NavGraphBuilder.${NORMALIZED_NAME}Screen() {
    composable(route = ${NORMALIZED_NAME^^}_ROUTE) {
        ${PASCAL_NAME}Screen()
    }
}
EOF
fi

# 7. settings.gradle.kts 에 추가
SETTINGS_FILE="settings.gradle.kts"
INCLUDE_LINE="include(\":$TYPE:$NAME\")"

if grep -Fxq "$INCLUDE_LINE" "$SETTINGS_FILE"; then
    echo "⚠️  settings.gradle.kts에 이미 등록되어 있습니다."
else
    echo "" >> "$SETTINGS_FILE"
    echo "$INCLUDE_LINE" >> "$SETTINGS_FILE"
    echo "✅  settings.gradle.kts에 include 추가 완료"
fi

echo "✨  모듈 및 파일 생성 완료! Gradle Sync를 실행해주세요."
