[![Kotlin](https://img.shields.io/badge/kotlin-2.0.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![License](https://img.shields.io/badge/license-GNU%20GPL%20v3.0-blue)](LICENSE.txt)
![Platform](https://img.shields.io/badge/platform-CLI-lightgrey.svg)

> A command-line tool to automate the extraction of assets from Figma and generate `.xcassets` catalogs for seamless integration into Xcode projects.

# Getting Started

## Installation
### Build from source code

Clone the repository:

```bash
git clone https://github.com/cschindler/figma-xcassets.git
cd figma-xcassets
```

Build and run the project:

```bash
./gradlew run -q --args="config.json"
```

### Downloading the Binary from GitHub Releases

1. Go to the [Releases page](https://github.com/cschindler/figma-xcassets/releases).
2. Find the latest stable version in the list of available releases.
3. Once the file is downloaded, extract it to your desired location.
4. (Optional) Add the binary to your `PATH` environment variable to easily access it from anywhere in the terminal.

## Figma API Token

To authenticate with the Figma API, you need to add your Figma API token to the environment variables.

Add the token to your environment variables:

```bash
export FIGMA_API_TOKEN="your-figma-api-token"
```

## Configuration

The configuration file (`config.json`) defines how assets are exported from Figma.

### Example

```json
{
  "outputName": "MyAssets",
  "outputPath": "./export",
  "outputFormat": "xcassets",
  "resources": [
    {
      "fileKey": "abc123XYZ",
      "pageName": "DesignSystem",
      "layerName": "Icons-Large",
      "includePatterns": ["icon-[a-z]+-large(?:-light|-dark)?"],
      "excludePatterns": ["icon-exclude-this-one"],
      "exportOptions": {
        "format": "png",
        "scales": [1, 2, 3]
      },
      "maxDepth": 3,
      "readyToDevOnly": true,
      "outputFolderName": "Large"
    },
    {
      "fileKey": "abc123XYZ",
      "pageName": "DesignSystem",
      "layerName": "Icons-Small",
      "includePatterns": ["icon-[a-z]+-small(?:-light|-dark)?"],
      "excludePatterns": ["icon-exclude-this-one"],
      "exportOptions": {
        "format": "png",
        "scales": [1, 2, 3]
      },
      "maxDepth": 3,
      "readyToDevOnly": true,
      "outputFolderName": "Small"
    }
  ],
  "xcassets": {
    "appearances": {
      "mode": "suffix",
      "lightPattern": "-light",
      "darkPattern": "-dark"
    }
  }
}
```

### Top-level

| Key               | Type                | Description                                                     |
|-------------------|---------------------|-----------------------------------------------------------------|
| `outputName`      | `String`            | Name of the exported folder. Default: `"Assets"`                |
| `outputPath`      | `String`            | Output directory path. Default: `"."`                           |
| `outputFormat`    | `OutputFormat`      | Defines the output format for the assets. Default: `"xcassets"` |
| `resources`       | `List<Resource>`    | List of resources to fetch and export                           |
| `xcassets`        | `XcassetsSettings?` | Optional settings for `.xcassets` export                        |

### OutputFormat

| Key        | Type            | Description                                                      |
|------------|-----------------|------------------------------------------------------------------|
| `raw`      | `Enum`          | The assets will be exported as individual files.                 |
| `xcassets` | `Enum`          | The assets will be bundled into `.xcassets` directory structure. |


### Resource

| Key                | Type                | Description                                                           |
|--------------------|---------------------|-----------------------------------------------------------------------|
| `fileKey`          | `String`            | Figma file key                                                        |
| `pageName`         | `String`            | Figma page name                                                       |
| `layerName`        | `String?`           | Optional layer name                                                   |
| `includePatterns`  | `List<String>`      | List of patterns to include (regular expressions)                     |
| `excludePatterns`  | `List<String>`      | List of patterns to exclude (regular expressions)                     |
| `exportOptions`    | `ExportOptions`     | Options for exporting (format, scales)                                |
| `maxDepth`         | `Int`               | Max depth for layers to export. Default: `3`                          |
| `readyToDevOnly`   | `Boolean`           | Whether to export only assets ready for development. Default: `false` |
| `outputFolderName` | `String?`           | Optional output folder name within the resource.                      |

### ExportOptions

| Key           | Type                | Description                                 |
|---------------|---------------------|---------------------------------------------|
| `format`      | `Format`            | The export format. One of `"svg"`, `"png"`. |
| `scales`      | `List<Int>`         | List of scale values. Default: `1`          |

### XcassetsSettings

| Key             | Type               | Description                                                                 |
|-----------------|--------------------|-----------------------------------------------------------------------------|
| `appearances`   | `Appearances?`     | Optional settings for handling light/dark appearances                       |

### Appearances

| Key              | Type                | Description                                                             |
|------------------|---------------------|-------------------------------------------------------------------------|
| `mode`           | `AppearanceMode`    | How to handle appearance: `"suffix"` or `"prefix"`. Default: `"suffix"` |
| `lightPattern`   | `String?`           | Pattern for light appearance.                                           |
| `darkPattern`    | `String?`           | Pattern for dark appearance.                                            |

## Testing

Unit tests can be run with:

```bash
./gradlew test
```

# Contributing

Contributions are welcome! Feel free to open issues or submit pull requests.

# License

This project is licensed under the [GPL-3.0 license](LICENSE.txt).
