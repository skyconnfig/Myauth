"""
MyAuth Python License 验证库
支持 Flask 和 FastAPI 框架
使用 RSA-SHA256 签名验证
"""

import json
import base64
import hashlib
from datetime import datetime, date
from typing import Optional, Dict, Any
from dataclasses import dataclass, asdict

try:
    from cryptography.hazmat.primitives import hashes, serialization
    from cryptography.hazmat.primitives.asymmetric import padding, rsa
    from cryptography.hazmat.backends import default_backend
    from cryptography.exceptions import InvalidSignature
except ImportError:
    raise ImportError("请安装 cryptography 库: pip install cryptography")


@dataclass
class License:
    """License 授权实体"""
    license_key: str = ""
    expire_time: str = ""  # 格式: yyyy-MM-dd
    machine_id: str = ""  # 可选，用于绑定设备
    type: str = "trial"  # trial, standard, professional, enterprise
    max_users: int = 1
    modules: str = "[]"  # JSON 数组字符串
    customer_name: str = ""
    remark: str = ""


class LicenseCodec:
    """License 编码/解码工具类"""
    
    @staticmethod
    def encode(data: str, sign: str) -> str:
        """
        编码：将授权数据和签名组合成 License Key
        格式: Base64(data.signature)
        """
        combined = f"{data}.{sign}"
        return base64.b64encode(combined.encode('utf-8')).decode('utf-8')
    
    @staticmethod
    def decode(license_key: str) -> tuple:
        """
        解码：从 License Key 中分离出授权数据和签名
        返回: (data, sign)
        """
        try:
            decoded = base64.b64decode(license_key).decode('utf-8')
            dot_index = decoded.rfind('.')
            if dot_index == -1:
                raise ValueError("无效的 License Key 格式")
            data = decoded[:dot_index]
            sign = decoded[dot_index + 1:]
            return data, sign
        except Exception as e:
            raise ValueError(f"License Key 解码失败: {str(e)}")


class LicenseValidator:
    """
    License 验证器
    
    使用方法:
        validator = LicenseValidator(public_key_path="license/public.key")
        license_obj = validator.validate(license_key)
    """
    
    def __init__(self, public_key_base64: str = None, public_key_path: str = None):
        """
        初始化验证器
        
        Args:
            public_key_base64: Base64 格式的公钥
            public_key_path: 公钥文件路径（PEM 格式）
        """
        if public_key_base64:
            self.public_key = self._load_public_key_from_base64(public_key_base64)
        elif public_key_path:
            self.public_key = self._load_public_key_from_file(public_key_path)
        else:
            raise ValueError("必须提供 public_key_base64 或 public_key_path")
    
    def _load_public_key_from_base64(self, base64_key: str) -> Any:
        """从 Base64 字符串加载公钥"""
        try:
            key_bytes = base64.b64decode(base64_key)
            public_key = serialization.load_der_public_key(
                key_bytes,
                backend=default_backend()
            )
            return public_key
        except Exception as e:
            raise ValueError(f"加载公钥失败: {str(e)}")
    
    def _load_public_key_from_file(self, file_path: str) -> Any:
        """从文件加载公钥（PEM 格式）"""
        try:
            with open(file_path, 'rb') as f:
                pem_data = f.read()
            public_key = serialization.load_pem_public_key(
                pem_data,
                backend=default_backend()
            )
            return public_key
        except FileNotFoundError:
            raise FileNotFoundError(f"公钥文件不存在: {file_path}")
        except Exception as e:
            raise ValueError(f"加载公钥文件失败: {str(e)}")
    
    def validate(self, license_key: str, check_machine_id: bool = True) -> License:
        """
        验证 License Key
        
        Args:
            license_key: License 密钥
            check_machine_id: 是否检查机器码
            
        Returns:
            License 对象
            
        Raises:
            ValueError: 验证失败时抛出异常
        """
        try:
            # 1. 解码
            json_data, signature = LicenseCodec.decode(license_key)
            
            # 2. 验签
            self._verify_signature(json_data, signature)
            
            # 3. 解析 JSON
            license_dict = json.loads(json_data)
            license_obj = License(**license_dict)
            
            # 4. 时间校验
            self._check_expire_time(license_obj.expire_time)
            
            # 5. 机器码校验
            if check_machine_id and license_obj.machine_id:
                self._check_machine_id(license_obj.machine_id)
            
            return license_obj
            
        except ValueError:
            raise
        except Exception as e:
            raise ValueError(f"License 验证失败: {str(e)}")
    
    def _verify_signature(self, data: str, signature: str) -> None:
        """验证 RSA 签名"""
        try:
            signature_bytes = base64.b64decode(signature)
            self.public_key.verify(
                signature_bytes,
                data.encode('utf-8'),
                padding.PKCS1v15(),
                hashes.SHA256()
            )
        except InvalidSignature:
            raise ValueError("授权验证失败：签名无效")
        except Exception as e:
            raise ValueError(f"签名验证失败: {str(e)}")
    
    def _check_expire_time(self, expire_time: str) -> None:
        """检查过期时间"""
        try:
            expire_date = datetime.strptime(expire_time, '%Y-%m-%d').date()
            today = date.today()
            if today > expire_date:
                raise ValueError(f"授权已过期，过期时间：{expire_time}")
        except ValueError as e:
            if "授权已过期" in str(e):
                raise
            raise ValueError(f"日期格式错误: {expire_time}，应为 yyyy-MM-dd 格式")
    
    def _check_machine_id(self, expected_machine_id: str) -> None:
        """检查机器码"""
        current_machine_id = self._get_machine_id()
        if expected_machine_id != current_machine_id:
            raise ValueError(
                f"设备不匹配，当前机器码：{current_machine_id}，"
                f"授权机器码：{expected_machine_id}"
            )
    
    @staticmethod
    def _get_machine_id() -> str:
        """获取当前机器码（基于硬件信息生成 MD5）"""
        import platform
        import uuid
        
        # 收集硬件信息
        info = []
        info.append(platform.node())  # 主机名
        info.append(platform.machine())  # 机器类型
        info.append(str(uuid.getnode()))  # MAC 地址
        
        # 生成 MD5
        machine_info = "|".join(info)
        machine_id = hashlib.md5(machine_info.encode('utf-8')).hexdigest().upper()
        return machine_id
    
    @staticmethod
    def get_remaining_days(expire_time: str) -> int:
        """计算剩余天数"""
        expire_date = datetime.strptime(expire_time, '%Y-%m-%d').date()
        today = date.today()
        delta = expire_date - today
        return delta.days


class LicenseManager:
    """
    License 管理器（应用级单例）
    
    使用方法:
        manager = LicenseManager(public_key_path="license/public.key")
        manager.initialize()  # 应用启动时调用
    """
    
    def __init__(self, public_key_base64: str = None, public_key_path: str = None,
                 license_file: str = "license/license.key"):
        self.validator = LicenseValidator(
            public_key_base64=public_key_base64,
            public_key_path=public_key_path
        )
        self.license_file = license_file
        self.license: Optional[License] = None
        self.is_validated = False
    
    def initialize(self, license_key: str = None) -> License:
        """
        初始化并验证 License
        
        Args:
            license_key: License 密钥（如果不提供，则从文件读取）
            
        Returns:
            License 对象
        """
        if license_key:
            self.license = self.validator.validate(license_key)
        else:
            self.license = self._validate_from_file()
        
        self.is_validated = True
        return self.license
    
    def _validate_from_file(self) -> License:
        """从文件验证 License"""
        try:
            with open(self.license_file, 'r', encoding='utf-8') as f:
                license_key = f.read().strip()
            return self.validator.validate(license_key)
        except FileNotFoundError:
            raise FileNotFoundError(f"未找到授权文件：{self.license_file}")
    
    def check_license(self) -> bool:
        """
        检查 License 状态（可用于定时任务）
        
        Returns:
            bool: 是否有效
        """
        if not self.license:
            return False
        
        try:
            remaining_days = LicenseValidator.get_remaining_days(self.license.expire_time)
            return remaining_days >= 0
        except:
            return False
    
    def get_license_info(self) -> Dict[str, Any]:
        """获取 License 信息"""
        if not self.license:
            return {"status": "not_initialized"}
        
        remaining_days = LicenseValidator.get_remaining_days(self.license.expire_time)
        
        return {
            "status": "valid" if remaining_days >= 0 else "expired",
            "customer_name": self.license.customer_name,
            "type": self.license.type,
            "expire_time": self.license.expire_time,
            "remaining_days": remaining_days,
            "max_users": self.license.max_users,
            "modules": json.loads(self.license.modules),
            "machine_id": self.license.machine_id
        }
