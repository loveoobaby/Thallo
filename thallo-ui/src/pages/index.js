import styles from './index.css';
import { Row, Col } from 'antd';
import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Table, Divider, Tag, Card, Menu } from 'antd';
import { Modal, Button } from 'antd';
import request from '../utils/request';
import LineChart from '../components/chart/LineChart';

@connect(({ monitor, loading }) => ({
  monitor,
}))
class Index extends PureComponent {

  state = {
    currentContainer: undefined,
  };

  componentDidMount = () => {
    this.updateModal();
    this.timerID = setInterval(() => {
      this.updateModal();
    }, 15000);
  };

  componentWillUnmount() {
    this.timerID && clearInterval(this.timerID);
  }

  clickStopApp = () => {
    Modal.confirm({
      title: '确认',
      content: '确定要停止Thallo AM吗？',
      onOk: this.stopApp,
    });
  };

  stopApp = () => {
    const resPomise = request('/stop');
  };

  updateModal = () => {
    const { dispatch } = this.props;
    dispatch({
      type: 'monitor/queryMonitorInfo',

    });
  };


  render() {
    const columns = [{
      title: 'ContainerId',
      dataIndex: 'containerId',
      key: 'containerId',
      render: text => <a href="javascript:;">{text}</a>,
    }, {
      title: 'HostName',
      dataIndex: 'hostName',
      key: 'hostName',
    }, {
      title: 'Image',
      dataIndex: 'image',
      key: 'image',
    }, {
      title: 'Role',
      key: 'role',
      dataIndex: 'role',
    }, {
      title: 'Action',
      key: 'action',
      render: (text, record) => (
        <span>
      <Button type={'primary'} href="javascript:;">Log</Button>
    </span>
      ),
    }];

    const operations = (
      <div>
        <Button type={'primary'} style={{ marginRight: 30 }}>
          Add New Container
        </Button>
        <Button type={'danger'} onClick={this.clickStopApp}>
          Stop Application
        </Button>
      </div>
    );


    return (
      <div>
        <Row>
          <Col span={4}>
            <h1 className={styles.title}>Thallo</h1>
          </Col>
          <Col span={16}>
            <h1 className={styles.title}> {this.props.monitor.applicationId} </h1>
          </Col>
          <Col span={4}>
            <h1 className={styles.title}></h1>
          </Col>
        </Row>

        <Card
          title={<h1>All Containers</h1>}
          headStyle={{ textAlign: 'left' }}
          extra={operations}
        >
          <Table onRow={(record) => {
            return {
              onClick: () => {
                const currentContainer = record.containerId;
                this.setState({
                  currentContainer,
                }, this.updateModal);// 点击行
              },
            };
          }}
                 columns={columns} dataSource={this.props.monitor.containers}/>
        </Card>

        <Card
          title={<h1>Containers</h1>}
          headStyle={{ textAlign: 'left' }}
        >
          <Col span={12} key={'CPU'}>
            <Card title={'CPU'}>
              <LineChart
                optionData={this.props.monitor.cpu}
                id={'CPU' + Math.ceil(Math.random() * 1000)}
                title={'CPU'}
                areaColor={'#8280a9'}
                lineColor={'#8280a9'}
              />
            </Card>
          </Col>
          <Col span={12} key={'Memeory'}>
            <Card title={'Memeory'}>
              <LineChart
                optionData={this.props.monitor.memory}
                id={'Mem' + Math.ceil(Math.random() * 1000)}
                title={'Memory'}
                areaColor={'#66a7ad'}
                lineColor={'#66a7ad'}
              />
            </Card>
          </Col>
        </Card>

      </div>

    );
  }


};

export default Index;
