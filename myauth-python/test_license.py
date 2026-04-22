"""
License 验证库测试脚本

使用方法:
    python test_license.py
"""

import sys
import os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from license_validator import LicenseValidator, LicenseManager, License
from datetime import datetime, timedelta
import json
import base64

# ==================== 测试配置 ====================

# 注意：这里需要使用真实的公钥进行测试
# 可以从 Java 生成器获取，或运行 Java 项目生成测试密钥
TEST_PUBLIC_KEY_BASE64 = ""  # 在此填入测试公钥
TEST_LICENSE_KEY = ""  # 在此填入测试 License Key


def print_section(title):
    """打印分隔线"""
    print("\n" + "=" * 60)
    print(f"  {title}")
    print("=" * 60)


def test_license_codec():
    """测试编解码功能"""
    print_section("测试 1: License 编解码")
    
    from license_validator import LicenseCodec
    
    # 测试数据
    data = '{"expire_time": "2026-12-31", "type": "trial"}'
    sign = "test_signature_12345"
    
    # 编码
    encoded = LicenseCodec.encode(data, sign)
    print(f"✓ 编码成功: {encoded[:50]}...")
    
    # 解码
    decoded_data, decoded_sign = LicenseCodec.decode(encoded)
    print(f"✓ 解码成功")
    print(f"  数据: {decoded_data}")
    print(f"  签名: {decoded_sign}")
    
    # 验证
    assert decoded_data == data, "数据不匹配"
    assert decoded_sign == sign, "签名不匹配"
    print("✓ 编解码测试通过")


def test_machine_id():
    """测试机器码生成"""
    print_section("测试 2: 机器码生成")
    
    machine_id = LicenseValidator._get_machine_id()
    print(f"当前机器码: {machine_id}")
    print(f"长度: {len(machine_id)} 字符")
    print("✓ 机器码生成测试通过")


def test_remaining_days():
    """测试剩余天数计算"""
    print_section("测试 3: 剩余天数计算")
    
    # 未来日期
    future_date = (datetime.now() + timedelta(days=30)).strftime('%Y-%m-%d')
    days = LicenseValidator.get_remaining_days(future_date)
    print(f"过期时间: {future_date}")
    print(f"剩余天数: {days}")
    assert days > 0, "剩余天数应为正数"
    print("✓ 未来日期测试通过")
    
    # 过去日期
    past_date = (datetime.now() - timedelta(days=10)).strftime('%Y-%m-%d')
    days = LicenseValidator.get_remaining_days(past_date)
    print(f"\n过期时间: {past_date}")
    print(f"剩余天数: {days}")
    assert days < 0, "剩余天数应为负数"
    print("✓ 过去日期测试通过")


def test_license_validation():
    """测试 License 验证（需要真实密钥）"""
    print_section("测试 4: License 验证")
    
    if not TEST_PUBLIC_KEY_BASE64 or not TEST_LICENSE_KEY:
        print("⚠  跳过：未配置测试密钥")
        print("提示：设置 TEST_PUBLIC_KEY_BASE64 和 TEST_LICENSE_KEY 以运行此测试")
        return
    
    try:
        validator = LicenseValidator(public_key_base64=TEST_PUBLIC_KEY_BASE64)
        license_obj = validator.validate(TEST_LICENSE_KEY)
        
        print(f"✓ License 验证成功")
        print(f"  客户: {license_obj.customer_name}")
        print(f"  类型: {license_obj.type}")
        print(f"  过期时间: {license_obj.expire_time}")
        print(f"  最大用户数: {license_obj.max_users}")
    except Exception as e:
        print(f"✗ License 验证失败: {e}")


def test_license_manager():
    """测试 License 管理器"""
    print_section("测试 5: License 管理器")
    
    if not TEST_PUBLIC_KEY_BASE64:
        print("⚠  跳过：未配置测试公钥")
        return
    
    try:
        manager = LicenseManager(public_key_base64=TEST_PUBLIC_KEY_BASE64)
        
        # 检查初始化状态
        print(f"初始化状态: {manager.is_validated}")
        
        # 获取 License 信息
        info = manager.get_license_info()
        print(f"License 信息: {json.dumps(info, indent=2, ensure_ascii=False)}")
        
        print("✓ License 管理器测试通过")
    except Exception as e:
        print(f"✗ License 管理器测试失败: {e}")


def test_error_handling():
    """测试错误处理"""
    print_section("测试 6: 错误处理")
    
    # 测试无效 License Key
    try:
        validator = LicenseValidator(public_key_base64="invalid_key")
        print("✗ 应抛出异常")
    except Exception as e:
        print(f"✓ 正确捕获错误: {type(e).__name__}")
    
    # 测试格式错误的 Key
    try:
        validator = LicenseValidator(public_key_base64=TEST_PUBLIC_KEY_BASE64 or "dGVzdA==")
        validator.validate("invalid_license_format")
        print("✗ 应抛出异常")
    except Exception as e:
        print(f"✓ 正确捕获格式错误: {type(e).__name__}")


def demo_usage():
    """使用演示"""
    print_section("使用演示")
    
    print("""
基本使用流程:

1. 初始化验证器
   validator = LicenseValidator(public_key_path="license/public.key")

2. 验证 License
   license_obj = validator.validate(license_key)

3. 检查信息
   print(f"客户: {license_obj.customer_name}")
   print(f"类型: {license_obj.type}")
   print(f"过期时间: {license_obj.expire_time}")

4. 使用管理器（推荐）
   manager = LicenseManager(public_key_path="license/public.key")
   manager.initialize()  # 应用启动时调用
   
   # 定期检查
   if manager.check_license():
       print("License 有效")
   
   # 获取信息
   info = manager.get_license_info()
    """)


def main():
    """运行所有测试"""
    print("\n" + "🔐 MyAuth License 验证库测试")
    print("=" * 60)
    
    tests = [
        ("编解码功能", test_license_codec),
        ("机器码生成", test_machine_id),
        ("剩余天数计算", test_remaining_days),
        ("License 验证", test_license_validation),
        ("License 管理器", test_license_manager),
        ("错误处理", test_error_handling),
        ("使用演示", demo_usage),
    ]
    
    passed = 0
    failed = 0
    skipped = 0
    
    for name, test_func in tests:
        try:
            test_func()
            if "跳过" not in sys.stdout.getvalue() if hasattr(sys.stdout, 'getvalue') else False:
                passed += 1
            else:
                skipped += 1
        except AssertionError as e:
            print(f"\n✗ {name} 测试失败: {e}")
            failed += 1
        except Exception as e:
            print(f"\n✗ {name} 测试异常: {e}")
            failed += 1
    
    print_section("测试结果汇总")
    print(f"通过: {passed}")
    print(f"失败: {failed}")
    print(f"跳过: {skipped}")
    print(f"总计: {len(tests)}")
    
    if failed == 0:
        print("\n✅ 所有测试通过！")
    else:
        print(f"\n❌ {failed} 个测试失败")
    
    return failed == 0


if __name__ == '__main__':
    success = main()
    sys.exit(0 if success else 1)
