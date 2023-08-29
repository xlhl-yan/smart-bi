import {genChartByAiSyncUsingPOST, genChartsByAiUsingPOST} from '@/services/smart-bi/chartController';
import { UploadOutlined } from '@ant-design/icons';
import {
  Button,
  Card,
  Col,
  Divider,
  Form,
  Input,
  message,
  Row,
  Select,
  Space,
  Spin,
  Upload,
} from 'antd';
import TextArea from 'antd/es/input/TextArea';
import ReactECharts from 'echarts-for-react';
import React, { useState } from 'react';

/**
 * 添加图表页面
 * @constructor
 */
const AddChart: React.FC = () => {
  const [chart, setChart] = useState<JSON>();
  const [option, setOption] = useState<API.BiResponse>();
  const [loading, setLoading] = useState<boolean>(false);
  const onFinish = async (values: any) => {
    //  避免重复提交
    if (loading) {
      return;
    }
    setLoading(true);
    setOption(undefined);
    setChart(undefined);
    const params = {
      ...values,
      file: undefined,
    };
    try {
      //todo 文件上传到大小限制
      const res = await genChartByAiSyncUsingPOST(params, {}, values.file.file.originFileObj);
      if (!res?.data) {
        throw new Error(res?.message);
      }
      const genChart = JSON.parse(res.data.genChart ?? undefined);
      if (!genChart) {
        throw new Error(res?.message);
      } else {
        message.success('分析成功',5);
        setChart(genChart);
        setOption(res.data);
      }
    } catch (e) {
      message.error(e.message, 5);
    }
    setLoading(false);
  };
  return (
    <div className="add-chart">
      <Row gutter={24}>
        <Col span={12}>
          <Card title={'智能分析'}>
            <Form
              name="分析"
              wrapperCol={12}
              labelAlign={'left'}
              labelCol={6}
              onFinish={onFinish}
              initialValues={{}}
            >
              <Form.Item
                name="goal"
                label="分析目标"
                rules={[{ required: true, message: '请输入分析目标' }]}
              >
                <TextArea placeholder={'请输入你的分析诉求，例：分析网站用户变化趋势'} />
              </Form.Item>
              <Form.Item name="name" label="图表名称">
                <Input placeholder={'请输入你的图表名称'} />
              </Form.Item>

              <Form.Item name="chartType" label="图表类型" hasFeedback>
                <Select
                  placeholder="请输入你的分析目标"
                  options={[
                    { value: '折线图', label: '折线图' },
                    { value: '柱状图', label: '柱状图' },
                    { value: '雷达图', label: '雷达图' },
                    { value: '堆叠图', label: '堆叠图' },
                    { value: '饼图', label: '饼图' },
                  ]}
                />
              </Form.Item>

              <Form.Item name={'file'} valuePropName={'file'} label={'选择文件 excel 或 csv'}>
                <Upload name="file" listType="picture" maxCount={1}>
                  <Button icon={<UploadOutlined />}>上传文件(excel/csv)</Button>
                </Upload>
              </Form.Item>

              <Form.Item wrapperCol={{ span: 12, offset: 9 }}>
                <Space>
                  <Button type="primary" htmlType="submit" loading={loading} disabled={loading}>
                    提交
                  </Button>
                  <Button htmlType="reset" disabled={loading}>
                    重置
                  </Button>
                </Space>
              </Form.Item>
            </Form>
          </Card>
        </Col>
        <Col span={12}>
          <Card title={'分析结论：'}>
            <div>
              {option?.genResult ?? <div>请在左侧提交信息后查看</div>}
              {<Spin spinning={loading} />}
            </div>
          </Card>
          <Divider />
          <Card title={'生成图标：'}>
            <div>
              {chart ? <ReactECharts option={chart} /> : <div>请在左侧提交信息后查看</div>}
              {<Spin spinning={loading} />}
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  );
};
export default AddChart;
