import { genChartsByAiUsingPOST } from '@/services/smart-bi/chartController';
import { UploadOutlined } from '@ant-design/icons';
import { Button, Form, Input, message, Select, Space, Upload } from 'antd';
import TextArea from 'antd/es/input/TextArea';
import ReactECharts from 'echarts-for-react';
import React, { useState } from 'react';

/**
 * 添加图表页面
 * @constructor
 */
const AddChart: React.FC = () => {
  const [Chart, setChart] = useState<JSON>();
  const [Option, setOption] = useState<API.BiResponse>();
  const [Loading, setLoading] = useState<boolean>(false);
  const onFinish = async (values: any) => {
    //  避免重复提交
    if (Loading) {
      return;
    }
    setLoading(true);
    const params = {
      ...values,
      file: undefined,
    };
    try {
      //todo 文件上传到大小限制
      const res = await genChartsByAiUsingPOST(params, {}, values.file.file.originFileObj);
      console.log(res.data);
      if (!res?.data) {
        message.error('分析失败');
      }
      const genChart = JSON.parse(res.data.genChart ?? '');
      if (!genChart) {
        throw new Error('图表代码解析错误');
      } else {
        message.success('分析成功');
        setChart(genChart);
        setOption(res.data);
        console.log('Option:', Option);
      }
    } catch (e) {
      message.error('分析失败', e);
    }
    setLoading(false);
  };
  return (
    <div className="addChart">
      <Form name="分析" onFinish={onFinish} initialValues={{}}>
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
          <Upload name="file" listType="picture">
            <Button icon={<UploadOutlined />}>上传文件</Button>
          </Upload>
        </Form.Item>

        <Form.Item wrapperCol={{ span: 12, offset: 6 }}>
          <Space>
            <Button type="primary" htmlType="submit" loading={Loading} disabled={Loading}>
              提交
            </Button>
            <Button htmlType="reset" disabled={Loading}>
              重置
            </Button>
          </Space>
        </Form.Item>
      </Form>
      <div>
        分析结论：
        {Option?.genResult}
      </div>
      <div>
        生成图标：
        {Chart && <ReactECharts option={Chart} />}
      </div>
    </div>
  );
};
export default AddChart;
