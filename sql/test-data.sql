-- ============================================
-- 测试数据初始化脚本
-- 在 blog_system 数据库中执行
-- ============================================

USE `blog_system`;

-- 插入测试分类（先跑这个，分类不需要用户）
INSERT INTO `category` (`name`, `sort`, `status`) VALUES
('科技', 1, 0),
('财经', 2, 0),
('体育', 3, 0),
('娱乐', 4, 0),
('教育', 5, 0);

-- 注意：管理员用户请在应用中通过注册接口创建，然后手动改 role 为 1
-- 步骤：
-- 1. 在 Knife4j 中调用 POST /api/auth/register 注册一个用户
-- 2. 在 MySQL 中执行：UPDATE user SET role = 1 WHERE username = '你的用户名';
