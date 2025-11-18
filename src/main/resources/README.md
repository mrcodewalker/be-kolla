# Media Server API Documentation

## Overview
This documentation describes the media server API structure. Note: MediaSoup integration has been removed from this application.

## API Endpoints

### Room Management

#### 1. Join Room
- **URL**: `/rooms/:roomId`
- **Method**: GET
- **Description**: Get room's RTP capabilities
- **Response**: Router RTP capabilities object

#### 2. Create Broadcaster
- **URL**: `/rooms/:roomId/broadcasters`
- **Method**: POST
- **Request Body**:
```json
{
  "id": "string",
  "displayName": "string",
  "device": {
    "name": "string",
    "version": "string"
  },
  "rtpCapabilities": {}
}
```
- **Response**: Broadcaster data object

### Transport Management

#### 1. Create Transport
- **URL**: `/rooms/:roomId/broadcasters/:broadcasterId/transports`
- **Method**: POST
- **Request Body**:
```json
{
  "type": "webrtc|plain",
  "rtcpMux": boolean,
  "comedia": boolean,
  "sctpCapabilities": {}
}
```
- **Response**: Transport data object

#### 2. Connect Transport
- **URL**: `/rooms/:roomId/broadcasters/:broadcasterId/transports/:transportId/connect`
- **Method**: POST
- **Request Body**:
```json
{
  "dtlsParameters": {},
  "ip": "string",
  "port": number,
  "rtcpPort": number
}
```
- **Response**: Success status

### Producer Management

#### 1. Create Producer
- **URL**: `/rooms/:roomId/broadcasters/:broadcasterId/transports/:transportId/producers`
- **Method**: POST
- **Request Body**:
```json
{
  "kind": "audio|video",
  "rtpParameters": {},
  "appData": {
    "share": boolean,
    "recording": boolean
  }
}
```
- **Response**: Producer ID and data

### Consumer Management

#### 1. Create Consumer
- **URL**: `/rooms/:roomId/broadcasters/:broadcasterId/transports/:transportId/consume`
- **Method**: POST
- **Request Body**:
```json
{
  "producerId": "string",
  "rtpCapabilities": {},
  "paused": boolean
}
```
- **Response**: Consumer data object

### Recording API

#### 1. Start Recording
- **URL**: `/rooms/:roomId/record/start`
- **Method**: POST
- **Request Body**:
```json
{
  "producerId": "string",
  "recordingId": "string",
  "options": {
    "format": "webm|mp4",
    "videoCodec": "vp8|h264",
    "audioCodec": "opus",
    "outputPath": "string"
  }
}
```
- **Response**: Recording session info

#### 2. Stop Recording
- **URL**: `/rooms/:roomId/record/stop`
- **Method**: POST
- **Request Body**:
```json
{
  "recordingId": "string"
}
```
- **Response**: Recording metadata

## WebSocket Events

### Client -> Server
- `join`: Join a room
- `produce`: Start producing media
- `consume`: Start consuming media
- `startRecord`: Start recording
- `stopRecord`: Stop recording

### Server -> Client
- `producerScore`: Producer quality score updates
- `newPeer`: New peer joined notification
- `peerClosed`: Peer left notification
- `activeSpeaker`: Active speaker updates
- `recordingStarted`: Recording started notification
- `recordingStopped`: Recording stopped notification

## Data Transfer Objects (DTOs)

### RoomDTO
```typescript
interface RoomDTO {
  id: string;
  peers: PeerDTO[];
  routerRtpCapabilities: RtpCapabilities;
}
```

### PeerDTO
```typescript
interface PeerDTO {
  id: string;
  displayName: string;
  device: {
    name: string;
    version: string;
  };
  producers: ProducerDTO[];
  consumers: ConsumerDTO[];
}
```

### ProducerDTO
```typescript
interface ProducerDTO {
  id: string;
  kind: 'audio' | 'video';
  type: 'camera' | 'screen' | 'microphone';
  paused: boolean;
  score: number;
  rtpParameters: RtpParameters;
}
```

### ConsumerDTO
```typescript
interface ConsumerDTO {
  id: string;
  producerId: string;
  kind: 'audio' | 'video';
  rtpParameters: RtpParameters;
  paused: boolean;
}
```

### RecordingDTO
```typescript
interface RecordingDTO {
  id: string;
  roomId: string;
  producerId: string;
  startTime: string;
  endTime?: string;
  outputPath: string;
  format: 'webm' | 'mp4';
  status: 'recording' | 'completed' | 'failed';
}
```

## Spring Boot Integration

To integrate with Spring Boot, create the following controllers and services:

### Controllers
1. RoomController
2. BroadcasterController
3. TransportController
4. ProducerController
5. ConsumerController
6. RecordingController

### Services
1. RoomService
2. MediaService
3. RecordingService
4. WebSocketService

### Example Spring Boot Controller
```java
@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    
    @Autowired
    private RoomService roomService;
    
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDTO> getRoom(@PathVariable String roomId) {
        return ResponseEntity.ok(roomService.getRoom(roomId));
    }
    
    @PostMapping("/{roomId}/record/start")
    public ResponseEntity<RecordingDTO> startRecording(
        @PathVariable String roomId,
        @RequestBody StartRecordingRequest request
    ) {
        return ResponseEntity.ok(roomService.startRecording(roomId, request));
    }
}
```
