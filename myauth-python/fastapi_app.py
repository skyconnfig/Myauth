"""
FastAPI 集成示例 - MyAuth License 验证

安装依赖:
    pip install fastapi uvicorn cryptography

使用方法:
    uvicorn fastapi_app:app --reload --port 8000
"""

from fastapi import FastAPI, Depends, HTTPException, Request
from fastapi.responses import JSONResponse
from typing import Optional
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

# ==================== 初始化 FastAPI ====================

app = FastAPI(
    title="MyAuth License System",
    description="FastAPI + MyAuth License 验证示例",
    version="1.0.0"
)

# 全局 License 管理器
license_manager: Optional[LicenseManager] = None


@app.on_event("startup")
async def startup_event():
    """应用启动时验证 License"""
    global license_manager
    
    try:
        # 选择一种方式初始化
        if PUBLIC_KEY_BASE64 and "..." not in PUBLIC_KEY_BASE64:
            license_manager = LicenseManager(public_key_base64=PUBLIC_KEY_BASE64)
        else:
            license_manager = LicenseManager(
                public_key_path=PUBLIC_KEY_FILE,
                license_file=LICENSE_FILE
            )
        
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


# ==================== 依赖注入 ====================

async def verify_license(request: Request) -> dict:
    """
    License 验证依赖
    用于保护需要授权的路由
    """
    if not license_manager or not license_manager.is_validated:
        raise HTTPException(
            status_code=403,
            detail="未找到有效的 License，请联系管理员获取授权"
        )
    
    # 检查 License 状态
    if not license_manager.check_license():
        raise HTTPException(
            status_code=403,
            detail="License 已过期或无效"
        )
    
    # 返回 License 信息
    return license_manager.get_license_info()


async def verify_license_type(required_types: list = None):
    """
    验证 License 类型
    用于检查特定功能所需的授权级别
    """
    async def license_type_checker(request: Request, license_info: dict = Depends(verify_license)):
        if required_types and license_info.get('type') not in required_types:
            raise HTTPException(
                status_code=403,
                detail=f"此功能需要 {' 或 '.join(required_types)} 授权，当前为 {license_info.get('type')}"
            )
        return license_info
    return license_type_checker


def check_trial_mode(license_info: Optional[dict] = Depends(verify_license)):
    """
    试用模式检查
    允许在未授权情况下有限制地使用
    """
    if license_info:
        return license_info
    
    # 试用模式
    return {
        "status": "trial",
        "type": "trial",
        "max_users": 5,
        "remaining_days": 30,
        "modules": ["basic"]
    }


# ==================== 路由示例 ====================

@app.get("/")
async def index(license_info: dict = Depends(check_trial_mode)):
    """首页 - 试用模式可用"""
    return {
        "message": "欢迎使用 MyAuth 系统",
        "license_info": license_info
    }


@app.get("/api/data")
async def get_data(license_info: dict = Depends(verify_license)):
    """受保护的 API - 需要有效 License"""
    return {
        "message": "这是受保护的数据",
        "license_info": license_info,
        "data": {
            "users": ["user1", "user2"],
            "features": ["feature1", "feature2"]
        }
    }


@app.get("/api/premium-feature")
async def premium_feature(
    license_info: dict = Depends(verify_license_type(['professional', 'enterprise']))
):
    """高级功能 - 需要 professional 或 enterprise 授权"""
    return {
        "message": "访问高级功能成功",
        "data": "这是专业版功能",
        "license_info": license_info
    }


@app.get("/api/license/status")
async def license_status():
    """查看 License 状态（公开接口）"""
    if license_manager and license_manager.is_validated:
        return {
            "code": 200,
            "data": license_manager.get_license_info()
        }
    else:
        return {
            "code": 200,
            "data": {
                "status": "trial",
                "message": "试用模式",
                "remaining_days": 30
            }
        }


class LicenseValidateRequest(BaseModel):
    """License 验证请求模型"""
    license_key: str


from pydantic import BaseModel


@app.post("/api/license/validate")
async def validate_license(request: LicenseValidateRequest):
    """
    手动验证 License Key
    用于在线激活
    """
    if not request.license_key:
        raise HTTPException(status_code=400, detail="请提供 license_key")
    
    try:
        # 创建临时验证器
        if PUBLIC_KEY_BASE64 and "..." not in PUBLIC_KEY_BASE64:
            validator = LicenseManager(public_key_base64=PUBLIC_KEY_BASE64)
        else:
            validator = LicenseManager(public_key_path=PUBLIC_KEY_FILE)
        
        license_obj = validator.initialize(request.license_key)
        
        return {
            "code": 200,
            "message": "License 验证成功",
            "data": {
                "customer_name": license_obj.customer_name,
                "type": license_obj.type,
                "expire_time": license_obj.expire_time,
                "max_users": license_obj.max_users
            }
        }
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"License 验证失败: {str(e)}")


# ==================== 中间件示例 ====================

@app.middleware("http")
async def license_middleware(request: Request, call_next):
    """
    License 验证中间件
    可以对所有请求进行统一的 License 检查
    """
    # 排除某些路径不进行 License 检查
    excluded_paths = ["/", "/docs", "/openapi.json", "/api/license/status"]
    
    if request.url.path not in excluded_paths:
        # 可以在这里添加全局 License 检查逻辑
        pass
    
    response = await call_next(request)
    return response


# ==================== 后台任务示例 ====================

from fastapi import BackgroundTasks
import asyncio


async def periodic_license_check():
    """定期检查 License（每小时）"""
    while True:
        await asyncio.sleep(3600)  # 每小时检查一次
        if license_manager:
            is_valid = license_manager.check_license()
            if not is_valid:
                print("⚠️  License 已过期！")
                # 可以发送通知、记录日志等


@app.on_event("startup")
async def start_background_tasks():
    """启动后台任务"""
    # asyncio.create_task(periodic_license_check())
    pass


# ==================== 启动说明 ====================

if __name__ == '__main__':
    import uvicorn
    uvicorn.run(app, host='0.0.0.0', port=8000)
