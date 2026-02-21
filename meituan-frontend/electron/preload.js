const { contextBridge, ipcRenderer } = require('electron')

// 暴露安全的API给渲染进程
contextBridge.exposeInMainWorld('electronAPI', {
  // 文件操作相关API
  selectFile: () => ipcRenderer.invoke('select-file'),
  saveFile: (data) => ipcRenderer.invoke('save-file', data),
  
  // 窗口控制
  minimizeWindow: () => ipcRenderer.send('window-minimize'),
  maximizeWindow: () => ipcRenderer.send('window-maximize'),
  closeWindow: () => ipcRenderer.send('window-close'),
  
  // 系统信息
  platform: process.platform
})
