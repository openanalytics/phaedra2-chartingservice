-- noinspection

TRUNCATE charts.hca_chart_template RESTART IDENTITY CASCADE;
TRUNCATE charts.hca_setting RESTART IDENTITY CASCADE;

insert into charts.hca_chart_template(id, type, axis_x, axis_y, group_by, filter)
values (1000, 'bar', 'row', 'column', 'wellType', 'plateId = 2000');
insert into charts.hca_chart_template(id, type, axis_x, axis_y, group_by, filter)
values (2000, 'bar', 'row', 'column', 'wellType', 'plateId = 3000');

insert into charts.hca_setting(id, chart_template_id, setting_type, name, value)
values (1000, 1000, 'AXIS', 'size', '10');
insert into charts.hca_setting(id, chart_template_id, setting_type, name, value)
values (2000, 1000, 'AXIS', 'size', '20');
insert into charts.hca_setting(id, chart_template_id, setting_type, name, value)
values (3000, 1000, 'CHART', 'size', '30');
insert into charts.hca_setting(id, chart_template_id, setting_type, name, value)
values (4000, 1000, 'CHART', 'size', '40');