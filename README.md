## `contentment`
![Maven Central Version](https://img.shields.io/maven-central/v/me.rmyhal.contentment/contentment?style=flat&logo=sonatype)
![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/rmyhal/contentment/checks.yml)

`Contentment` prevents flickering and short-lived progress indicators to ensure a smoother user experience.
```kotlin
Contentment {
  when (uiState) {
    is Loading -> indicator { CircularProgressIndicator() }
    is Loaded -> content { ScreenContent(uiState) }
  }
}
```

| don’t | do | do2 |
| ----- | -- | -- |
| <img height=450 src="https://github.com/rmyhal/contentment/assets/8909650/51e68728-b50e-445f-b102-dc42af053abf"/> | <img height=450 src="https://github.com/user-attachments/assets/a3787c53-e645-41cf-9dec-1a365d25ad37"/> | <img height=450 src="https://github.com/rmyhal/contentment/assets/8909650/2ab290aa-215e-4343-9161-12803a0677c0"/> |


## Usage

```groovy
implementation "me.rmyhal.contentment:contentment:2.0.1"
```

The `Contentment` composable manages the transition between loading and loaded states, allows specifying minimum display time and delay before showing the loading indicator:
* If the content finishes loading before the `delayMillis` threshold, the `indicator {}` will not be shown.
* If the content loading exceeds the `delayMillis` threshold, the `indicator {}` will be displayed, 
and `content {}` will appear only after the `minShowTimeMillis` duration has passed. 

If your screen is built with a sealed UI state: 
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
      is Loaded -> content { LoadedContent(uiState) }
      // multiple content's are possible too
      is Failed -> content { FailedContent(uiState) }
    }
  }  
}
```

A direct adaptation of [ContentLoadingProgressBar](https://developer.android.com/reference/androidx/core/widget/ContentLoadingProgressBar). for Compose. Use this when you only need to wrap the indicator itself.

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
