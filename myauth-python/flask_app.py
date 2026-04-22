"""
Flask 集成示例 - MyAuth License 验证中间件

安装依赖:
    pip install flask cryptography

使用方法:
    python flask_app.py
"""

from flask import Flask, request, jsonify, g
from functools import wraps
import sys
import os

# 添加父目录到路径以便导入 license_validator
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from license_validator import LicenseManager, License

# ==================== 配置 ====================

# 方式1: 使用 Base64 公钥（推荐，从 Java 生成器获取）
PUBLIC_KEY_BASE64 = """
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...（你的公钥）
""".strip()

# 方式2: 使用公钥文件
PUBLIC_KEY_FILE = "license/public.key"
LICENSE_FILE = "license/license.key"

# ==================== 初始化 Flask ====================

app = Flask(__name__)

# 初始化 License 管理器
try:
    # 选择一种方式初始化
    if PUBLIC_KEY_BASE64 and "..." not in PUBLIC_KEY_BASE64:
        license_manager = LicenseManager(public_key_base64=PUBLIC_KEY_BASE64)
    else:
        license_manager = LicenseManager(public_key_path=PUBLIC_KEY_FILE, license_file=LICENSE_FILE)
    
    # 应用启动时验证 License
    license_info = license_manager.initialize()
    print(f"✅ License 验证成功!")
    print(f"   客户: {license_info.customer_name}")
    print(f"   类型: {license_info.type}")
    print(f"   过期时间: {license_info.expire_time}")
except Exception as e:
    print(f"❌ License 验证失败: {e}")
    print("⚠️  应用将以试用模式运行（30天限制）")
    license_manager = None


# ==================== License 验证装饰器 ====================

def require_license(f):
    """
    License 验证装饰器
    用于保护需要授权的路由
    """
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not license_manager or not license_manager.is_validated:
            return jsonify({
                "code": 403,
                "message": "未找到有效的 License，请联系管理员获取授权"
            }), 403
        
        # 检查 License 状态
        if not license_manager.check_license():
            return jsonify({
                "code": 403,
                "message": "License 已过期或无效"
            }), 403
        
        # 将 License 信息存入 g 对象，供视图函数使用
        g.license_info = license_manager.get_license_info()
        
        return f(*args, **kwargs)
    return decorated_function


def check_trial_mode(f):
    """
    试用模式检查装饰器
    允许在未授权情况下有限制地使用
    """
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if license_manager and license_manager.is_validated:
            # 已授权，直接通过
            g.license_info = license_manager.get_license_info()
            return f(*args, **kwargs)
        
        # 试用模式：限制功能
        g.license_info = {
            "status": "trial",
            "type": "trial",
            "max_users": 5,
            "remaining_days": 30,
            "modules": ["basic"]
        }
        
        # 可以在这里添加试用限制逻辑
        print("⚠️  当前为试用模式，功能受限")
        
        return f(*args, **kwargs)
    return decorated_function


# ==================== 路由示例 ====================

@app.route('/')
@check_trial_mode
def index():
    """首页 - 试用模式可用"""
    return jsonify({
        "message": "欢迎使用 MyAuth 系统",
        "license_info": g.license_info
    })


@app.route('/api/data')
@require_license
def get_data():
    """受保护的 API - 需要有效 License"""
    return jsonify({
        "message": "这是受保护的数据",
        "license_info": g.license_info,
        "data": {
            "users": ["user1", "user2"],
            "features": ["feature1", "feature2"]
        }
    })


@app.route('/api/premium-feature')
@require_license
def premium_feature():
    """高级功能 - 检查授权类型"""
    license_info = g.license_info
    
    # 检查是否为专业版或企业版
    if license_info.get('type') not in ['professional', 'enterprise']:
        return jsonify({
            "code": 403,
            "message": f"此功能需要 professional 或 enterprise 授权，当前为 {license_info.get('type')}"
        }), 403
    
    return jsonify({
        "message": "访问高级功能成功",
        "data": "这是专业版功能"
    })


@app.route('/api/license/status')
def license_status():
    """查看 License 状态（公开接口）"""
    if license_manager and license_manager.is_validated:
        return jsonify({
            "code": 200,
            "data": license_manager.get_license_info()
        })
    else:
        return jsonify({
            "code": 200,
            "data": {
                "status": "trial",
                "message": "试用模式",
                "remaining_days": 30
            }
        })


@app.route('/api/license/validate', methods=['POST'])
def validate_license():
    """
    手动验证 License Key
    用于在线激活
    """
    data = request.get_json()
    license_key = data.get('license_key')
    
    if not license_key:
        return jsonify({
            "code": 400,
            "message": "请提供 license_key"
        }), 400
    
    try:
        # 创建临时验证器
        if PUBLIC_KEY_BASE64 and "..." not in PUBLIC_KEY_BASE64:
            validator_type = "base64"
            validator = LicenseManager(public_key_base64=PUBLIC_KEY_BASE64)
        else:
            validator_type = "file"
            validator = LicenseManager(public_key_path=PUBLIC_KEY_FILE)
        
        license_obj = validator.initialize(license_key)
        
        return jsonify({
            "code": 200,
            "message": "License 验证成功",
            "data": {
                "customer_name": license_obj.customer_name,
                "type": license_obj.type,
                "expire_time": license_obj.expire_time,
                "max_users": license_obj.max_users
            }
        })
    except Exception as e:
        return jsonify({
            "code": 400,
            "message": f"License 验证失败: {str(e)}"
        }), 400


# ==================== 定时任务示例（可选）====================

def schedule_license_check():
    """
    定时检查 License（每小时）
    可以使用 APScheduler 或其他定时任务库
    """
    import threading
    import time
    
    def check_loop():
        while True:
            time.sleep(3600)  # 每小时检查一次
            if license_manager:
                is_valid = license_manager.check_license()
                if not is_valid:
                    print("⚠️  License 已过期！")
                    # 可以发送通知、记录日志等
    
    thread = threading.Thread(target=check_loop, daemon=True)
    thread.start()


# ==================== 启动应用 ====================

if __name__ == '__main__':
    # 启动定时检查（可选）
    # schedule_license_check()
    
    app.run(host='0.0.0.0', port=5000, debug=True)
