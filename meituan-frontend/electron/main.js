import { app, BrowserWindow, Menu, ipcMain, globalShortcut } from 'electron'
import { fileURLToPath } from 'url'
import { dirname, join } from 'path'
import { existsSync } from 'fs'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

let mainWindow

function createWindow() {
  // 隐藏默认菜单栏
  Menu.setApplicationMenu(null)

  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    minWidth: 1000,
    minHeight: 600,
    // 隐藏窗口标题栏
    frame: false,
    backgroundColor: '#f5f5f5',
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      preload: join(__dirname, 'preload.js')
    }
  })

  // 开发环境加载Vite服务器
  if (process.env.NODE_ENV === 'development') {
    const loadDevServer = async () => {
      try {
        await mainWindow.loadURL('http://localhost:5173')
        mainWindow.webContents.openDevTools()
      } catch (err) {
        console.log('等待 Vite 服务器启动...')
        setTimeout(loadDevServer, 1000)
      }
    }
    loadDevServer()
  } else {
    // 生产环境加载构建后的文件
    const indexPath = join(__dirname, '../dist/index.html')
    
    // 详细的路径信息日志
    console.log('=== Production Mode - Loading Application ===')
    console.log('Current __dirname:', __dirname)
    console.log('Process cwd:', process.cwd())
    console.log('Index path:', indexPath)
    console.log('File exists:', existsSync(indexPath))
    
    // 检查文件是否存在
    if (!existsSync(indexPath)) {
      console.error('ERROR: index.html not found at:', indexPath)
      console.error('Please ensure the application was built correctly with: npm run build')
    }
    
    // 加载文件并处理错误
    mainWindow.loadFile(indexPath).catch(err => {
      console.error('=== Failed to Load Application ===')
      console.error('Error:', err)
      console.error('Attempted path:', indexPath)
      console.error('Error message:', err.message)
      console.error('Error stack:', err.stack)
    })
    
    // 生产环境也打开开发者工具以便调试
    mainWindow.webContents.openDevTools()
    
    // 监听渲染进程的控制台消息
    mainWindow.webContents.on('console-message', (event, level, message, line, sourceId) => {
      const levelMap = ['DEBUG', 'INFO', 'WARN', 'ERROR']
      console.log(`[Renderer ${levelMap[level]}]:`, message)
      if (line && sourceId) {
        console.log(`  at ${sourceId}:${line}`)
      }
    })
    
    // 监听渲染进程崩溃
    mainWindow.webContents.on('crashed', (event, killed) => {
      console.error('=== Renderer Process Crashed ===')
      console.error('Killed:', killed)
    })
    
    // 监听渲染进程无响应
    mainWindow.on('unresponsive', () => {
      console.warn('=== Window Unresponsive ===')
    })
  }

  // 注册全局快捷键 Ctrl+Shift+D 来切换开发者工具
  globalShortcut.register('CommandOrControl+Shift+D', () => {
    if (mainWindow) {
      if (mainWindow.webContents.isDevToolsOpened()) {
        mainWindow.webContents.closeDevTools()
      } else {
        mainWindow.webContents.openDevTools()
      }
    }
  })

  mainWindow.on('closed', () => {
    mainWindow = null
  })
}

// 窗口控制 IPC 监听器
ipcMain.on('window-minimize', () => {
  if (mainWindow) {
    mainWindow.minimize()
  }
})

ipcMain.on('window-maximize', () => {
  if (mainWindow) {
    if (mainWindow.isMaximized()) {
      mainWindow.unmaximize()
    } else {
      mainWindow.maximize()
    }
  }
})

ipcMain.on('window-close', () => {
  if (mainWindow) {
    mainWindow.close()
  }
})

app.whenReady().then(createWindow)

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow()
  }
})

// 应用退出时注销所有快捷键
app.on('will-quit', () => {
  globalShortcut.unregisterAll()
})
