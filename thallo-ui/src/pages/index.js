import styles from './index.less';
import { Row, Col } from 'antd';
import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Table, Divider, Tag, Card, Menu, Icon } from 'antd';
import { Modal, Button } from 'antd';
import request from '../utils/request';
import LineChart from '../components/chart/LineChart';
import AddContainer from '../components/AddContainer/index';

@connect(({ monitor, loading }) => ({
  monitor,
}))
class Index extends PureComponent {

  state = {
    currentContainer: undefined,
    showAddContainer: false,
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
      payload: {
        currentContainer: this.state.currentContainer,
      },
    });
  };

  addNewContainer = () => {
    this.setState({
      showAddContainer: true,
    });
  };

  closeNewContainer = () => {
    this.setState({
      showAddContainer: false,
    });
  };


  open_new_window(url) {
    const w = window.open('about:blank');
    w.location.href = url;
  }


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
      <Button type={'primary'} onClick={() => {
        this.open_new_window('http://' + this.props.monitor.rmWebHost + '/cluster/container/' + record.containerId);
      }}>Log</Button>
    </span>
      ),
    }];


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

        <div className={styles.userHead}>
          <div className={styles.search}>
            <h2>All Conatiners</h2>
          </div>
          <div className={styles.btn}>
            <Button type="primary" onClick={this.addNewContainer}>
              Add New Container
            </Button>
            <Button type="danger" onClick={this.clickStopApp}>
              Stop Application
            </Button>
          </div>
        </div>

        <Table onRow={(record) => {
          return {
            onClick: () => {
              const currentContainer = record.containerId;
              this.setState({
                currentContainer,
              }, this.updateModal);// 点击行
            },
          };
        }} columns={columns} dataSource={this.props.monitor.containers}/>

        <Card
          title={<h3>{this.props.monitor.currentContainer}</h3>}
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
        <AddContainer show={this.state.showAddContainer} onCancle={this.closeNewContainer}/>
      </div>

    );
  }


};

export default Index;
