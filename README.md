## `contentment`
![Maven Central Version](https://img.shields.io/maven-central/v/me.rmyhal.contentment/contentment?style=flat&logo=sonatype)
![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/rmyhal/contentment/checks.yml)

Are you fed up with progress indicators that run for 159 milliseconds and then disappears and are a real pain in the neck for both you and your customers?
`Contentment` effectively manages frustrating progress indicators, enhancing the user experience.
```kotlin
Contentment {
  when (uiState) {
    is Loading -> indicator { CircularProgressIndicator() }
    is Loaded -> content { ScreenContent(uiState) }
  }
}
```

| don’t | do |
| ---- | -- |
| <img height=450 src="https://github.com/rmyhal/contentment/assets/8909650/51e68728-b50e-445f-b102-dc42af053abf"/> | <img height=450 src="https://github.com/rmyhal/contentment/assets/8909650/2ab290aa-215e-4343-9161-12803a0677c0"/> |


## Usage

```groovy
implementation "me.rmyhal.contentment:contentment:<version>"
```

The library handles content loading with customizable behavior for showing loading indicators.
Allows specifying minimum display time and delay before showing the loading indicator.
* If the content finishes loading before the `delayMillis` threshold, the `indicator {}` will not be shown.
* If the content loading exceeds the `delayMillis` threshold, the `indicator {}` will be displayed, 
and `content {}` will appear only after the `minShowTimeMillis` duration has passed. 

If you screen is built with a sealed UI state. 
```kotlin
@Composable
fun Screen(viewModel: ViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ScreenContent(uiState)
}

@Composable
fun ScreenContent(uiState: UiState) {
  Contentment(
    minShowTimeMillis = 700L,
    delayMillis = 500L
  ) {
    when (uiState) {
      is Loading -> indicator { CircularProgressIndicator() }
      is Loaded -> content { ScreenContent(uiState) }
    }
  }  
}
```

Apart from that, the library provides a Jetpack Compose adaptation of the native [ContentLoadingProgressBar](https://developer.android.com/reference/androidx/core/widget/ContentLoadingProgressBar).

```kotlin
var loading = remember { mutableStateOf(true) }
ContentLoadingIndicator(
  loading = loading,
) {
  CircularProgressIndicator()
}
```

## License 

```
Copyright 2024 Ruslan Myhal

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
