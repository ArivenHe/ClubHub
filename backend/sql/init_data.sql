-- Optional seed data for software recommendations
INSERT INTO biz_software_tool (name, category, download_url, description, recommended_by, created_at)
SELECT 'Notion', '文档协作', 'https://www.notion.so', '用于社团知识库与会议记录沉淀', '系统初始化', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_software_tool WHERE name = 'Notion');

INSERT INTO biz_software_tool (name, category, download_url, description, recommended_by, created_at)
SELECT 'Draw.io', '流程图', 'https://app.diagrams.net', '免费在线画图，适合活动流程和分工图', '系统初始化', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_software_tool WHERE name = 'Draw.io');
