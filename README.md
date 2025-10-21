# Video Processing with FFmpeg Integration

This project provides comprehensive video processing capabilities including video download with resume support, playback progress tracking, and frame extraction using FFmpeg.

## Features

### 🎬 Video Processing
- **Video Download**: Download videos with resume capability and progress tracking
- **Metadata Extraction**: Get comprehensive video information (duration, resolution, codecs, etc.)
- **Progress Tracking**: Real-time playback progress simulation with frame-level accuracy
- **Frame Extraction**: Extract frames at specific timestamps or intervals
- **Thumbnail Generation**: Create video thumbnails and preview frames

### 🛠️ Core Components

#### FFmpegConfig
Enhanced version of your original class with additional capabilities:
- Video download with progress callbacks
- Frame extraction at specific timestamps
- Video metadata analysis using FFprobe
- Thumbnail and preview generation

#### VideoService
High-level service for video operations:
- Playback simulation with progress tracking
- Frame extraction management
- Video summary generation
- Session management for multiple videos

#### Model Classes
- **VideoProgress**: Tracks playback state, current time, frames, etc.
- **VideoFrame**: Represents extracted frame information
- **VideoMetadata**: Comprehensive video file information

## Prerequisites

### FFmpeg Installation
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install ffmpeg

# macOS (using Homebrew)
brew install ffmpeg

# Windows (using Chocolatey)
choco install ffmpeg
```

### Java Requirements
- Java 17 or higher
- Maven 3.6 or higher

## Usage Examples

### Basic Video Download
```java
String videoUrl = "https://example.com/video.mp4";
Path destination = Paths.get("downloaded-video.mp4");

// Simple download
FFmpegConfig.downloadVideo(videoUrl, destination);

// Download with resume capability
FFmpegConfig.downloadVideo(videoUrl, destination, true);

// Download with progress tracking
FFmpegConfig.downloadVideo(videoUrl, destination, true, progress -> {
    System.out.println("Download progress: " + progress + "%");
});
```

### Video Metadata Analysis
```java
Path videoPath = Paths.get("video.mp4");
VideoMetadata metadata = FFmpegConfig.getVideoMetadata(videoPath);

System.out.println("Duration: " + metadata.getFormattedDuration());
System.out.println("Resolution: " + metadata.getResolution());
System.out.println("Frame Rate: " + metadata.getFrameRate() + " fps");
System.out.println("File Size: " + metadata.getFormattedFileSize());
```

### Frame Extraction
```java
Path videoPath = Paths.get("video.mp4");

// Extract frame at 30 seconds
VideoFrame frame = FFmpegConfig.extractFrameAtTime(
    videoPath, 30.0, Paths.get("frame_30s.jpg"));

// Extract frames at 10-second intervals
List<VideoFrame> frames = FFmpegConfig.extractFramesAtIntervals(
    videoPath, 10.0, Paths.get("frames/"));

// Generate thumbnail (at 10% of video duration)
VideoFrame thumbnail = FFmpegConfig.generateThumbnail(
    videoPath, Paths.get("thumbnail.jpg"));
```

### Video Progress Tracking
```java
VideoService videoService = new VideoService();

// Start playback simulation
VideoProgress progress = videoService.startVideoPlayback(videoPath);

// Get current progress
VideoProgress current = videoService.getPlaybackProgress(progress.getVideoId());
System.out.println("Current time: " + current.getFormattedCurrentTime());
System.out.println("Progress: " + current.getProgressPercentage() + "%");

// Seek to specific time
videoService.seekToTime(progress.getVideoId(), 120.0); // Seek to 2 minutes

// Extract frame at current position
VideoFrame currentFrame = videoService.extractCurrentFrame(
    progress.getVideoId(), Paths.get("current_frame.jpg"));
```

### Video Summary Generation
```java
VideoService videoService = new VideoService();

// Generate comprehensive video summary
VideoService.VideoSummary summary = videoService.createVideoSummary(
    videoPath, Paths.get("output/"));

System.out.println("Summary: " + summary.getSummaryInfo());
System.out.println("Thumbnail: " + summary.getThumbnail().getFramePath());
System.out.println("Preview frames: " + summary.getTotalPreviewFrames());
```

## Running the Demo

1. **Build the project:**
```bash
mvn clean compile
```

2. **Run the interactive demo:**
```bash
mvn spring-boot:run
```

3. **Interactive commands:**
- `p` - Play/pause video
- `s <time>` - Seek to specific time (seconds)
- `f` - Extract frame at current position
- `t` - Generate thumbnails
- `q` - Quit demo

## Configuration

### FFmpeg Paths
Update the paths in `FFmpegConfig.java` if FFmpeg is not in your system PATH:
```java
private static final String FFMPEG_PATH = "/usr/bin/ffmpeg";
private static final String FFPROBE_PATH = "/usr/bin/ffprobe";
```

### Output Directories
The demo creates the following directories:
- `target/` - Downloaded videos
- `target/frames/` - Extracted frames
- `target/thumbnails/` - Generated thumbnails

## API Reference

### Key Methods

#### FFmpegConfig
- `downloadVideo(url, destination, resume, progressCallback)` - Download with progress
- `getVideoMetadata(videoPath)` - Extract video metadata
- `extractFrameAtTime(videoPath, timestamp, outputPath)` - Extract single frame
- `extractFramesAtIntervals(videoPath, interval, outputDir)` - Extract multiple frames
- `generateThumbnail(videoPath, thumbnailPath)` - Generate video thumbnail

#### VideoService
- `downloadVideoWithProgress(url, destination, progressCallback)` - Managed download
- `startVideoPlayback(videoPath)` - Start progress tracking
- `seekToTime(videoId, timeSeconds)` - Seek to position
- `extractCurrentFrame(videoId, outputPath)` - Extract frame at current position
- `createVideoSummary(videoPath, outputDir)` - Generate comprehensive summary

## Error Handling

The system handles various error conditions:
- Missing FFmpeg installation (falls back to system PATH)
- Network interruptions during download (resume capability)
- Invalid video files or timestamps
- File system errors (creates directories as needed)

## Performance Notes

- Frame extraction is CPU-intensive; consider batch processing
- Large videos may require significant disk space for frame extraction
- Progress tracking uses minimal resources (1-second intervals)
- Metadata is cached to avoid repeated FFprobe calls

## License

This project is provided as-is for educational and development purposes.