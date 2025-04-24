// Handle file operations
document.addEventListener('DOMContentLoaded', function() {
    // Upload functionality
    const uploadBtn = document.getElementById('upload-btn');
    const uploadModal = document.getElementById('upload-modal');
    const uploadForm = document.getElementById('upload-form');
    const closeBtn = document.querySelector('.close');
    const cancelUpload = document.getElementById('cancel-upload');
    const uploadProgress = document.getElementById('upload-progress');
    const progressFill = document.querySelector('.progress-fill');
    const progressText = document.querySelector('.progress-text');
    const transferSpeed = document.querySelector('.transfer-speed');
    const fileName = document.getElementById('file-name');
    const cancelTransfer = document.getElementById('cancel-transfer');
    
    if (uploadBtn) {
        uploadBtn.addEventListener('click', function() {
            uploadModal.style.display = 'block';
        });
    }
    
    if (closeBtn) {
        closeBtn.addEventListener('click', function() {
            uploadModal.style.display = 'none';
            resetUploadForm();
        });
    }
    
    if (cancelUpload) {
        cancelUpload.addEventListener('click', function() {
            uploadModal.style.display = 'none';
            resetUploadForm();
        });
    }
    
    if (uploadForm) {
        uploadForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const fileInput = document.getElementById('file-input');
            const roomSelect = document.getElementById('room-select');
            const encryptionType = document.getElementById('encryption-type');
            
            if (fileInput.files.length === 0) {
                alert('Please select a file to upload.');
                return;
            }
            
            const file = fileInput.files[0];
            fileName.textContent = file.name;
            
            // Show progress UI
            uploadForm.style.display = 'none';
            uploadProgress.style.display = 'block';
            
            // Create form data
            const formData = new FormData();
            formData.append('file', file);
            formData.append('encryptionType', encryptionType.value);
            
            if (roomSelect.value) {
                formData.append('roomId', roomSelect.value);
                uploadToRoom(formData);
            } else {
                uploadFile(formData);
            }
        });
    }
    
    // Download functionality
    const downloadButtons = document.querySelectorAll('.download-btn, .action-btn.download');
    const downloadModal = document.getElementById('download-modal');
    const downloadFileName = document.getElementById('download-file-name');
    const downloadProgressFill = document.querySelector('#download-modal .progress-fill');
    const downloadProgressText = document.querySelector('#download-modal .progress-text');
    const downloadSpeed = document.querySelector('#download-modal .transfer-speed');
    
    downloadButtons.forEach(button => {
        button.addEventListener('click', function() {
            const fileId = this.getAttribute('data-id');
            const fileName = this.closest('.file-item, .file-card').querySelector('h4').textContent;
            
            downloadFileName.textContent = fileName;
            downloadModal.style.display = 'block';
            
            downloadFile(fileId);
        });
    });
    
    // Close download modal
    const downloadCloseBtn = document.querySelector('#download-modal .close');
    if (downloadCloseBtn) {
        downloadCloseBtn.addEventListener('click', function() {
            downloadModal.style.display = 'none';
        });
    }
    
    // Helper functions
    function resetUploadForm() {
        uploadForm.reset();
        uploadForm.style.display = 'block';
        uploadProgress.style.display = 'none';
        progressFill.style.width = '0%';
        progressText.textContent = '0%';
        transferSpeed.textContent = '0 KB/s';
    }
    
    function uploadFile(formData) {
        const xhr = new XMLHttpRequest();
        let startTime = new Date().getTime();
        let lastLoaded = 0;
        let currentSpeed = 0;
        
        xhr.upload.addEventListener('progress', function(e) {
            if (e.lengthComputable) {
                const percent = Math.round((e.loaded / e.total) * 100);
                progressFill.style.width = percent + '%';
                progressText.textContent = percent + '%';
                
                // Calculate speed
                const currentTime = new Date().getTime();
                const elapsedTime = (currentTime - startTime) / 1000; // seconds
                
                if (elapsedTime > 0) {
                    currentSpeed = Math.round((e.loaded - lastLoaded) / elapsedTime);
                    lastLoaded = e.loaded;
                    startTime = currentTime;
                    
                    transferSpeed.textContent = formatSpeed(currentSpeed);
                }
            }
        });
        
        xhr.addEventListener('load', function() {
            if (xhr.status === 200) {
                const response = JSON.parse(xhr.responseText);
                setTimeout(function() {
                    alert('File uploaded successfully!');
                    uploadModal.style.display = 'none';
                    resetUploadForm();
                    window.location.reload(); // Refresh to show the new file
                }, 500);
            } else {
                alert('Error uploading file: ' + xhr.statusText);
                resetUploadForm();
            }
        });
        
        xhr.addEventListener('error', function() {
            alert('Error uploading file. Please try again.');
            resetUploadForm();
        });
        
        xhr.open('POST', '/api/files/upload', true);
        xhr.send(formData);
        
        // Cancel upload
        cancelTransfer.addEventListener('click', function() {
            xhr.abort();
            uploadModal.style.display = 'none';
            resetUploadForm();
        });
    }
    
    function uploadToRoom(formData) {
        const roomId = formData.get('roomId');
        const xhr = new XMLHttpRequest();
        let startTime = new Date().getTime();
        let lastLoaded = 0;
        
        xhr.upload.addEventListener('progress', function(e) {
            if (e.lengthComputable) {
                const percent = Math.round((e.loaded / e.total) * 100);
                progressFill.style.width = percent + '%';
                progressText.textContent = percent + '%';
                
                // Calculate speed
                const currentTime = new Date().getTime();
                const elapsedTime = (currentTime - startTime) / 1000; // seconds
                
                if (elapsedTime > 0) {
                    const currentSpeed = Math.round((e.loaded - lastLoaded) / elapsedTime);
                    lastLoaded = e.loaded;
                    startTime = currentTime;
                    
                    transferSpeed.textContent = formatSpeed(currentSpeed);
                }
            }
        });
        
        xhr.addEventListener('load', function() {
            if (xhr.status === 200) {
                const response = JSON.parse(xhr.responseText);
                setTimeout(function() {
                    alert('File uploaded to room successfully!');
                    uploadModal.style.display = 'none';
                    resetUploadForm();
                    window.location.reload(); // Refresh to show the new file
                }, 500);
            } else {
                alert('Error uploading file to room: ' + xhr.statusText);
                resetUploadForm();
            }
        });
        
        xhr.addEventListener('error', function() {
            alert('Error uploading file. Please try again.');
            resetUploadForm();
        });
        
        xhr.open('POST', `/api/share/upload/${roomId}`, true);
        xhr.send(formData);
        
        // Cancel upload
        cancelTransfer.addEventListener('click', function() {
            xhr.abort();
            uploadModal.style.display = 'none';
            resetUploadForm();
        });
    }
    
    function downloadFile(fileId) {
        // First get the transferId to track progress
        fetch(`/api/share/initDownload/${fileId}`)
            .then(response => response.json())
            .then(data => {
                const transferId = data.transferId;
                
                // Start the actual download
                window.location.href = `/api/share/download/${fileId}`;
                
                // Track progress in the background
                trackDownloadProgress(transferId);
            })
            .catch(error => {
                console.error('Error initiating download:', error);
                alert('Error initiating download. Please try again.');
                downloadModal.style.display = 'none';
            });
    }
    
    function trackDownloadProgress(transferId) {
        const progressInterval = setInterval(function() {
            fetch(`/api/share/progress/${transferId}`)
                .then(response => response.json())
                .then(data => {
                    const percent = Math.round(data.completionPercentage);
                    downloadProgressFill.style.width = percent + '%';
                    downloadProgressText.textContent = percent + '%';
                    downloadSpeed.textContent = formatSpeed(data.transferRate);
                    
                    if (percent >= 100) {
                        clearInterval(progressInterval);
                        setTimeout(function() {
                            downloadModal.style.display = 'none';
                        }, 1000);
                    }
                })
                .catch(error => {
                    console.error('Error tracking download progress:', error);
                    clearInterval(progressInterval);
                });
        }, 500);
        
        // Allow canceling tracking
        document.getElementById('cancel-download').addEventListener('click', function() {
            clearInterval(progressInterval);
            downloadModal.style.display = 'none';
        });
    }
    
    function formatSpeed(bytesPerSecond) {
        if (bytesPerSecond < 1024) {
            return bytesPerSecond + ' B/s';
        } else if (bytesPerSecond < 1048576) {
            return (bytesPerSecond / 1024).toFixed(1) + ' KB/s';
        } else {
            return (bytesPerSecond / 1048576).toFixed(1) + ' MB/s';
        }
    }
});