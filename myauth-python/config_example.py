"""
配置示例文件 - 复制为 config.py 并修改配置

使用方法:
    cp config_example.py config.py
    # 编辑 config.py 填入实际配置
"""

import os

class Config:
    """应用配置"""
    
    # ==================== License 配置 ====================
    
    # 方式1: Base64 公钥（推荐）
    # 从 Java 管理界面或 license/public.key 文件获取
    PUBLIC_KEY_BASE64 = os.getenv(
        'MYAUTH_PUBLIC_KEY',
        ''  # 在此填入你的公钥 Base64 字符串
    )
    
    # 方式2: 公钥文件路径
    PUBLIC_KEY_FILE = os.getenv(
        'MYAUTH_PUBLIC_KEY_FILE',
        'license/public.key'
    )
    
    # License 文件路径
    LICENSE_FILE = os.getenv(
        'MYAUTH_LICENSE_FILE',
        'license/license.key'
    )
    
    # ==================== 运行模式 ====================
    
    # 是否强制验证 License（False 允许试用模式）
    ENFORCE_LICENSE = os.getenv('MYAUTH_ENFORCE_LICENSE', 'false').lower() == 'true'
    
    # 试用期天数（仅在未授权时有效）
    TRIAL_DAYS = int(os.getenv('MYAUTH_TRIAL_DAYS', '30'))
    
    # 试用模式最大用户数
    TRIAL_MAX_USERS = int(os.getenv('MYAUTH_TRIAL_MAX_USERS', '5'))
    
    # ==================== 检查配置 ====================
    
    # 定时检查间隔（秒）
    CHECK_INTERVAL = int(os.getenv('MYAUTH_CHECK_INTERVAL', '3600'))  # 默认1小时
    
    # 启动时验证
    VALIDATE_ON_STARTUP = os.getenv('MYAUTH_VALIDATE_ON_STARTUP', 'true').lower() == 'true'
    
    # ==================== 日志配置 ====================
    
    # 是否记录验证日志
    LOG_VERIFICATION = os.getenv('MYAUTH_LOG_VERIFICATION', 'true').lower() == 'true'
    
    # 日志文件路径
    LOG_FILE = os.getenv('MYAUTH_LOG_FILE', 'logs/license.log')
    
    # ==================== Flask 配置 ====================
    
    FLASK_HOST = os.getenv('FLASK_HOST', '0.0.0.0')
    FLASK_PORT = int(os.getenv('FLASK_PORT', '5000'))
    FLASK_DEBUG = os.getenv('FLASK_DEBUG', 'false').lower() == 'true'
    
    # ==================== FastAPI 配置 ====================
    
    FASTAPI_HOST = os.getenv('FASTAPI_HOST', '0.0.0.0')
    FASTAPI_PORT = int(os.getenv('FASTAPI_PORT', '8000'))
    FASTAPI_RELOAD = os.getenv('FASTAPI_RELOAD', 'false').lower() == 'true'


class DevelopmentConfig(Config):
    """开发环境配置"""
    FLASK_DEBUG = True
    FASTAPI_RELOAD = True
    ENFORCE_LICENSE = False  # 开发环境不强制验证


class ProductionConfig(Config):
    """生产环境配置"""
    FLASK_DEBUG = False
    FASTAPI_RELOAD = False
    ENFORCE_LICENSE = True  # 生产环境强制验证
    LOG_VERIFICATION = True


# 根据环境变量选择配置
config_map = {
    'development': DevelopmentConfig,
    'production': ProductionConfig,
    'default': Config
}

env = os.getenv('MYAUTH_ENV', 'default')
config = config_map.get(env, Config)
