
# Demo app to showcase the exif interface fix for webp 

##Issue

When using https://developer.android.com/jetpack/androidx/releases/exifinterface the EXIF data isn't read in correctly causing rotation information not to be loaded. This bug was found after I noticed that coil wasn't respecting the orientation in webp files. Their jpg counter parts did rotate correctly.

Notes:
1. It's able to find the EXIF section but does not take into account the "Exif App1 Section"
2. This causes the the issue where it can't read the TIFF header correctly and fails to retrieve the byte-order information (which means it gives up on reading the exif metadata section)
3. After skipping 6 bytes of the Exif APP1 section and adjusting the payload, it will correctly process the EXIF section.

### Hexdump

| Google WebP image with exif                       | WebP image with Exif App1 section                   |
|---------------------------------------------------|-----------------------------------------------------|
| <img src="google-exif-hexdump.png" width="400" /> | <img src="laurence-exif-hexdump.png" width="400" /> |

Observation: Google's test data contains a WebP image without an Exif App1 section.

Helpful notes that helped me debug: https://stackoverflow.com/a/8227753

### Result
| Before                                    | After                                       |
|-------------------------------------------|---------------------------------------------|
| <img src="google-exif.png" width="300" /> | <img src="laurence-exif.png" width="300" /> |
