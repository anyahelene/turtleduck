import jsQR from 'jsqr';
import { assign } from 'lodash';
//import QrScanner from 'qr-scanner';
//import QrScannerWorkerPath from '!!file-loader!../../../../node_modules/qr-scanner/qr-scanner-worker.min.js';
//QrScanner.WORKER_PATH = QrScannerWorkerPath;


class Media {
	constructor() {
		this.mediaData = {};
	}
	
	add(data) {
		if(typeof(data) === "string") {
			this.mediaData[id] = data;
		} else if(data instanceof Blob || data instanceof File || data instanceof MediaSource) {
			this.mediaData[id] = URL.createObjectURL(data);
		}
	}
	get(id) {
		return this.mediaData[id];
	}
	
	remove(id) {
		const data = this.mediaData[id];
		delete this.mediaData[id];
		if(data) {
			URL.revokeObjectURL(data);
		}
	}
	
	clear() {
		for(const id in this.mediaData) {
			this.remove(id);
		}
	}
}

const media = new Media();
const cam_subscriptions = {};

class Camera {
	
	constructor() {
		this.btnHandler = e => this._buttonClicked(e);
		this.element = document.createElement('div');
		this.element.classList.add('camera');
		
		this.canvas = document.createElement('canvas');
		this.canvas.classList.add('preview');
		this.canvas.dataset.action = 'ready';
		this.canvas.addEventListener('click', this.btnHandler);
		this.canvas.width = 640;
		this.canvas.height = 480;
		this.element.appendChild(this.canvas);
		
		this.toolbars = {};
		this.toolbar = document.createElement('div');
		this.element.appendChild(this.toolbar);
		this.topbar = document.createElement('nav');
		this.topbar.classList.add('toolbar');
		this.topbar.classList.add('top');
		this.element.appendChild(this.topbar);

		['camera', 'qr', 'preview'].forEach(mode => {
			const elt = document.createElement('nav');
			elt.classList.add('toolbar');
			elt.style.display = 'none';
			elt.dataset.mode = mode;
			this.toolbar.appendChild(elt);
			this.toolbars[mode] = elt;
		});
		
		this.topbar.appendChild(this._makeButton('qr', 'camera preview-camera qr-switch', 'QR mode'))
		this.topbar.appendChild(this._makeButton('camera', 'qr preview-qr camera-switch', 'Camera mode'))
		this.topbar.appendChild(this._makeButton('close', 'close always', 'Close camera'))

		this.toolbars['camera'].appendChild(this._makeButton('snap', 'snap camera', 'Take picture'))

		this.outputElt = document.createElement('pre');
		//this._setText('Waiting for camera...')
		this.element.appendChild(this.outputElt);
		this._setMode('init');			
		this.ready = false;
	}

	_makeButton(action,style,text) {
		const btn = document.createElement('button');
		btn.dataset.action = action;
		btn.className = style;
		btn.type = 'button';
		btn.innerHTML = '<span>'+text+'</span>';
		btn.addEventListener('click', this.btnHandler);
		return btn;
	}
	_setClass(cls) {
		this.element.className = 'camera ' + cls;
	}
	static addSubscription(id, dest, mode, text, icon, title) {
		cam_subscriptions[id] = {id:id, dest:dest, mode:mode, text:text, icon:icon, title:title};
	}
	static removeSubscription(id) {
		delete cam_subscriptions[id];
	}
	_updatePreviewToolbar(mode) {
		const bar = this.toolbars['preview'];
		bar.querySelectorAll('button').forEach(elt => elt.remove());
		for(const id in cam_subscriptions) {
			const s = cam_subscriptions[id];
			console.log(mode, s);
			if(s.mode === mode) {
				const btn = document.createElement('button');
				btn.type = 'button';
				if(s.icon) {
					const ielt = document.createElement('span');
					ielt.classList.add('icon');
					ielt.textContent = s.icon;
					btn.appendChild(ielt);
				}
				if(s.text) {
					const telt = document.createElement('span');
					telt.textContent = s.text;
					btn.appendChild(telt);
				}
				if(s.title) {
					btn.title = s.title;
				}
				btn.classList.add('always');
				btn.dataset.action = 'send';
				btn.dataset.id = s.id;
				btn.dataset.dest = s.dest;
				btn.dataset.text = s.text;
				btn.dataset.icon = s.icon;
				btn.addEventListener('click', this.btnHandler);
				bar.appendChild(btn);			
				console.log(btn);	
			}
		}		
	}

	hide() {
		if(this.element) 
			this.element.style.display = 'none';
		if(this.parent)
			this.parent.classList.remove('active');
	}
	
	show() {
		if(this.element) 
			this.element.style.display = 'block';
		if(this.parent)
			this.parent.classList.add('active');
	}
	
	hidePreview() {
		if(this.canvas)
			this.canvas.style.display = 'none';
	}
	
	showPreview() {
		if(this.canvas)
			this.canvas.style.display = 'block';
	}
	
	hideVideo() {
		if(this.media)
			this.media.hide();
	}
	
	showVideo() {
		if(this.media)
			this.media.show();
	}
	isPlaying() {
		return this.media && this.media.isPlaying();
	}
	_buttonClicked(e) {
		const target = e.currentTarget;
		const action = target.dataset.action;
		console.log(target, action, e);
		if(action === 'snap') {
			if(this.snapshot()) {
				if(this.config.preview) {
					this._setMode('preview-' + this.mode);
				}
				if(this._snapHandlerOnce) {
					const handler = this._snapHandlerOnce;
					this._snapHandlerOnce = undefined;
					this.canvas.toBlob(handler, config.mimeType);
				} else if (this._snapHandler) {
					this.canvas.toBlob(this._snapHandler, config.mimeType);					
				}
			}
		} else if(action === 'close') {
			this.dispose();
		} else if(action === 'ready') { 
			this._reset();
		} else if(action === 'camera') {
			this.start('camera');
		} else if(action === 'qr') {
			this.start('qr');
		} else if(action === 'send') {
			const control = this;
			if(target.dataset.dest === 'builtin') {
				if(target.dataset.id === 'copy') {
					if(this.mode.endsWith('qr')) {
						navigator.clipboard.writeText(this.outputElt.textContent)
							.then(() => {
								turtleduck.userlog("Copied!");
								this._reset();
								if(this.config.once)
									this.dispose();
								},
								err => {
									turtleduck.userlog("Copy failed :(");
									console.error("Copy failed:", err);
								});
					}else if(this.mode.endsWith('camera')) {
						this.canvas.toBlob(data => 
							navigator.clipboard.write([new ClipboardItem({
									[data.type]: data
								})]).then(() => {
										turtleduck.userlog("Copied!");
										this._reset();
										if(this.config.once)
											this.dispose();
									},err => {
										turtleduck.userlog("Copy failed :()");
										console.error("Copy failed:", err);
									}));
					}
				} else if(target.dataset.id.startsWith('qpaste:')) {
					const dest = target.dataset.id.replace('qpaste:', '');
					if(turtleduck[dest]) {
						try {
							turtleduck[dest].paste("'" + this.outputElt.textContent + "'");
							turtleduck.userlog("Pasted!");
							this._reset();
								if(this.config.once)
									this.dispose();
						} catch(e) {
							turtleduck.userlog("Paste failed :(");
							console.error(e);						
						}
					}
				}
				console.log(target.dataset.id, target.dataset.mode);
			} else {
				const msg = {
						header: {
							to: target.dataset.dest,
							msg_type: target.dataset.id,
							msg_id: 'foo'
						},
						content: {
							data: '<data>',
							mode:this.mode.replace('preview-', '')
						}
					};
				if(this.mode.endsWith('qr')) {
					msg.content.data = this.outputElt.textContent;
					msg.content.type = 'text/plain';
					msg.content.name = 'qrdata';
					turtleduck.pyController.postMessage(msg);
					if(control.config.once)
						control.dispose();
				} else if(this.mode.endsWith('camera')) {
					this.canvas.toBlob(data => data.arrayBuffer().then(buf => {
						msg.content.url = URL.createObjectURL(data);
						msg.content.data = buf;
						msg.content.type = this.config.mimeType;
						console.log('send', msg);

						turtleduck.pyController.postMessage(msg, [buf]);
						if(this.config.once)
							this.dispose();

					}), this.config.mimeType);
				}
			}
		}
		e.preventDefault();
		e.stopPropagation();
	}
	attach(elt) {
		elt.appendChild(this.element);
		this.parent = elt;
	}
	dispose() {
		if(this.parent) {
			this.parent.removeChild(this.element);
		}
		if(this.media) {
			this.media.dispose();
			this.media = undefined;
		}
	}
	pause() {
		if(this.media) {
			this.media.pause();
		}
	}
	play() {
		if(this.media) {
			this.media.play();
		}
	}
	snapshot() {
		if(this.media) {
			return this.media.snapshot(this.canvas);
		} else {
			return false;
		}
	}
	once(mode = this.config.mode) {
		this._oldMode = this.mode;
		this.mode = mode;
		return this._setMode(mode);		
	}
	start(mode = this.config.mode) {
		this.mode = mode;
		this.play();
		return this._setMode(mode);		
	}
	
	_setMode(mode) {
		//if(!this.media) {
		//	console.warn('setMode before initialize');
		//	return;
		//}
		console.log("_setMode", mode);
		this._setClass(mode+'-mode');
		const m = mode.startsWith('preview') ? 'preview' : mode;
		for(const tm in this.toolbars) {
			console.log('m=', tm, this.toolbars[tm], tm === m ? 'flex' : 'none');
			this.toolbars[tm].style.display = tm === m ? 'flex' : 'none';
		}
		if(mode === 'camera') {
			this._setText('');
			this.hidePreview();
			this.showVideo();
			this.play();			
		} else if(mode === 'preview-camera') {
			this._updatePreviewToolbar('camera');
			this.showPreview();
			this.hideVideo();
			this.pause();			
		} else if(mode === 'qr') {
			this._setText('');
			this.showPreview();
			this.hideVideo();
			this.play();
			this._start_qrScan();
		} else if(mode === 'preview-qr') {
			this._updatePreviewToolbar('qr');
			this.showPreview();
			this.hideVideo();
			this.pause();		
		} else if(mode === 'init') {
			this._setText('Waiting for camera...');
			const ctx = this.canvas.getContext("2d");
			ctx.fillStyle = '#000';
			ctx.fillRect(0,0,this.canvas.width,this.canvas.height);
			this.showPreview();	
			this.hideVideo();
		} else {
			throw new Error("unknown camera mode: " + mode);
		}
	}

	_setText(text = '') {
		this.outputElt.textContent = text;
		this.outputElt.style.display = text ? 'block' : 'none';
	}
	initialize(config = {}) {
		if(this.media && this.config.fake !== config.fake) {
			this.media.dispose();
			this.media = undefined;
		}
		this.config = assign({
			mode: 'camera',
			video: true,
			preview:true,
			audio: false,
			once: false,
			mirror: true,
			fake: undefined,
			mimeType:'image/png'
		}, config);

		if(this.media) {
			this.start();
			return Promise.resolve(this);
		}
		this._setMode('init');
		if(config.fake) {
			this.media = new StaticMedia(config.fake, this, () => this.start(), (e) => {this._setText("Error starting camera: " + e)});
			console.log("initMedia(fake)", this.media);
			return Promise.resolve(this);
		} else if(Camera.hasMedia()) {
			return navigator.mediaDevices.getUserMedia({
				video: this.config.video,
				audio: this.config.audio
			}).then(stream => {
					this.media = new VideoMedia(stream, this, () => this.start(), (e) => {this._setText("Error starting camera: " + e)});
					console.log("initMedia", this.media);
					
					return Promise.resolve(this);
				})
				.catch(err => {
					this._setText(err);
					console.error(err);
				});			
		} else {
			this._setText("Media devices unavailable 😞");
			return Promise.reject(new Error("media devices unavailable"));
		}		
	}
	
	_reset() {
		if(this._oldMode) {
			this.mode = this._oldMode;
		}
		if(this.mode) {
			this._setMode(this.mode);
		}	
	}
	static hasMedia() {
		return !!(navigator.mediaDevices && navigator.mediaDevices.getUserMedia);
	}
	
	static getCamera() {
		if(Media.hasMedia()) {
			navigator.mediaDevices.getUserMedia({video:true,audio:false})
				.then(stream => new VideoMedia(stream))
				.catch(err => console.error(err));
		}
	}

	_start_qrScan() {
		this._frame = 0;
		const control = this;
		function scanner() {
			if(!control.isPlaying() || control.mode !== 'qr' || control.scanner != scanner) {
				console.log("QR scanner stopped")
				return;
			}
			if(control.media.isReady()) {
				if(control._do_qrScan()) {
					//return;
				}
			} else {
				console.log('QR scanner: video not ready');
			}
			// otherwise, try again
			requestAnimationFrame(scanner);
		}
		this.scanner = scanner;
		requestAnimationFrame(scanner);
	}

	_do_qrScan() {
		if(this.media.snapshot(this.canvas)) {
			this._frame = (this._frame || 0) + 1;
			const ctx = this.canvas.getContext("2d");
			if(this.config.mirror) {
				ctx.translate(this.canvas.width,0);
				ctx.scale(-1, 1);
			}
			const imgData = ctx.getImageData(0,0,this.canvas.width,this.canvas.height);
			const result = jsQR(imgData.data, imgData.width, imgData.height, {inversionAttempts: 'dontInvert'});
			if(result && result.data != '') {
				const loc = result.location;
				ctx.lineWidth = 3;
				ctx.strokeStyle = '#4aff00';
				drawPath(ctx, [loc.topLeftCorner, loc.topRightCorner,
					loc.bottomRightCorner, loc.bottomLeftCorner]);
				ctx.lineWidth = 2;
				ctx.strokeStyle = '#ffdf00';
				drawCircle(ctx, loc.topLeftFinderPattern, 3);
				drawCircle(ctx, loc.topRightFinderPattern, 3);
				drawCircle(ctx, loc.bottomRightAlignmentPattern, 2);
				drawCircle(ctx, loc.bottomLeftFinderPattern, 3);
				console.log("Found QR Code: ", result);
				this._setMode('preview-qr');
				this._setText(result.data);
				return result;
			} else {
				ctx.lineWidth = 1;
				ctx.strokeStyle = '#4aff00';
				//ctx.save();
				//ctx.translate(this.canvas.width/2, this.canvas.height/2);
				//ctx.rotate((Math.random()-.5)/20);
				//ctx.translate(-this.canvas.width/2, -this.canvas.height/2);
				//if(Math.round(this._frame/15) % 3 !== 0)
				drawTarget(ctx, this.canvas.width, this.canvas.height, .4);
				//ctx.restore();
			}
		} else {
			console.log("no snapshot");
		}		
		return undefined;
	}
}

function drawTarget(ctx, width, height, ratio) {
	const size = Math.min(width, height);
	const offset = size * ratio;
	const x0 = (width/2)-offset, x1 = (width/2)+offset;
	const y0 = (height/2)-offset, y1 = (height/2)+offset;
	const d = offset/2;
	ctx.beginPath();
	
	ctx.moveTo(x0,y0+d);
	ctx.lineTo(x0,y0);
	ctx.lineTo(x0+d,y0);
	
	ctx.moveTo(x1-d,y0);
	ctx.lineTo(x1,y0);
	ctx.lineTo(x1,y0+d);
	
	ctx.moveTo(x1,y1-d);
	ctx.lineTo(x1,y1);
	ctx.lineTo(x1-d,y1);
	
	ctx.moveTo(x0+d,y1);
	ctx.lineTo(x0,y1);
	ctx.lineTo(x0,y1-d);
	
	ctx.stroke();
}
function drawPath(ctx, points) {
	ctx.beginPath();
	points.forEach((p,i) => {
		if(i == 0) {
			ctx.moveTo(p.x, p.y);
		} else {
			ctx.lineTo(p.x, p.y);
		}
	})
	ctx.closePath();
	ctx.stroke();
}

function drawCircle(ctx, point, radius) {
	ctx.beginPath();
	ctx.arc(point.x, point.y, radius, 0, 2*Math.PI);
	ctx.stroke();
}
class VideoMedia {
	constructor(stream, control, start, error) {
		this.control = control;
		this.stream = stream;
		this.video = document.createElement('video');
		this.video.srcObject = stream;
		this.video.setAttribute('playsinline', true);
		this.control.element.appendChild(this.video);
		this.video.style.visibility = 'hidden';
		if(control.config.mirror)
			this.video.style.transform = 'scale(-1,1)';
		this.video.addEventListener('canplay', e => {
			console.log("video canplay", e);
			this.video.style.visibility = 'visible';
			this.ready = true;
			start();
		});
		this.video.addEventListener('error', e => {
			console.log("video error", e);
			error('video error');
		});
	}
	isPlaying() {
		return this.video && !this.video.paused;
	}
	isReady() {
		return this.video && this.video.readyState === this.video.HAVE_ENOUGH_DATA;
	}
	isStatic() {
		return false;
	}
	dispose() {
		if(this.stream) {
			this.stream.getTracks().forEach(t => t.stop())
			this.steam = undefined;
		}
		if(this.video) {
			this.video.remove();
			this.video = undefined;
		}
	}
	pause() {
		if(this.video) {
			this.video.pause();
		}
	}
	play() {
		if(this.video) {
			this.video.play();
		}
	}
	hide() {
		if(this.video)
			this.video.style.display = 'none';
	}
	show() {
		if(this.video)
			this.video.style.display = 'block';
	}

	snapshot(canvas) {
		if(!this.video)
			return false;
		const width = this.video.videoWidth, height = this.video.videoHeight;
		if(!(width && height))
			return false;
		canvas.width = width;
		canvas.height = height;
		const ctx = canvas.getContext("2d");
		if(this.control.config.mirror) {
			ctx.translate(canvas.width,0);
			ctx.scale(-1, 1);
		}
		ctx.drawImage(this.video, 0, 0, width, height);
		return true;
	}
}
class StaticMedia {
	constructor(url, control, start, error) {
		this.control = control;
		this.image = document.createElement('img');
		this.image.addEventListener('load', e => {
			console.log("image loaded", e);
			start();
		});
		this.image.addEventListener('error', e => {
			error(`'${url}' not found`);
		});
		this.image.src = url;
		this.control.element.appendChild(this.image);
		this.playing = false;
		this.frame = 0;
		if(control.config.mirror)
			this.image.style.transform = 'scale(-1,1)';
	}
	isPlaying() {
		return this.playing;
	}
	isReady() {
		return true;
	}
	isStatic() {
		return true;
	}
	dispose() {
		if(this.image) {
			this.image.remove();
			this.image = undefined;
		}
	}
	pause() {
		this.playing = false;
	}
	play() {
		this.playing = true;
		this.frame =0;
	}
	hide() {
		if(this.image)
			this.image.style.display = 'none';
	}
	show() {
		if(this.image)
			this.image.style.display = 'block';
	}

	snapshot(canvas) {
		if(!this.image) {
			console.log("no video");
			return false;
		}
		const width = this.image.naturalWidth, height = this.image.naturalHeight;
		if(!(width && height))
			return false;
		//canvas.width = width;
		//canvas.height = height;
		const ctx = canvas.getContext("2d");
		if(this.control.config.mirror) {
			ctx.translate(canvas.width,0);
			ctx.scale(-1, 1);
		}
		ctx.fillStyle = '#000';
		ctx.fillRect(0,0,canvas.width,canvas.height);
		
		// zoom in/out to ratio=1, then stop the "video"
		var z = 0;
		if(this.control.mode === 'qr') {
			z = this.frame / 30;
			if(z > 2)
				this.playing = false;
			if(z > 1)
				z = 2 - z;
		}
		this.frame = this.frame + 1;
		
		const ratio  = (1-z)*Math.min( canvas.width / width, canvas.height / height )+z;
		const w = width*ratio, h = height*ratio;
		console.log("drawImage", this.image, 0, 0, (canvas.width-w)/2, (canvas.height-h)/2, w, h);
		ctx.drawImage(this.image, 0, 0, width, height, (canvas.width-w)/2, (canvas.height-h)/2, w, h);
		return true;
	}
}

export { Camera, VideoMedia, StaticMedia };
